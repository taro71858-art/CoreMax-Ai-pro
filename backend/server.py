import os
from flask import Flask, request, jsonify
import requests

app = Flask(__name__)

# Gemini API Configurations
GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY", "")
GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

@app.route("/api/health", methods=["GET"])
def health_check():
    return jsonify({
        "status": "healthy",
        "brand": "CoreMaX AI",
        "service": "Business Analyst Assistant"
    })

@app.route("/api/calculate", methods=["POST"])
def calculate_metrics():
    """
    Validates and calculates financial indicators
    """
    data = request.json or {}
    try:
        revenue = float(data.get("revenue", 0))
        cogs = float(data.get("cogs", 0))
        opex = float(data.get("opex", 0))
        tax_rate = float(data.get("tax_rate", 6.0))

        gross_profit = revenue - cogs
        taxes = revenue * (tax_rate / 100.0)
        net_profit = gross_profit - opex - taxes

        gross_margin = (gross_profit / revenue * 100.0) if revenue > 0 else 0.0
        net_margin = (net_profit / revenue * 100.0) if revenue > 0 else 0.0
        markup = ((revenue - cogs) / cogs * 100.0) if cogs > 0 else 0.0

        contribution_margin_ratio = (gross_profit / revenue) if revenue > 0 else 0.0
        break_even_point = opex / contribution_margin_ratio if contribution_margin_ratio > 0 else 0.0

        return jsonify({
            "success": True,
            "metrics": {
                "revenue": revenue,
                "cogs": cogs,
                "opex": opex,
                "gross_profit": gross_profit,
                "net_profit": net_profit,
                "gross_margin_percent": round(gross_margin, 2),
                "net_margin_percent": round(net_margin, 2),
                "markup_percent": round(markup, 2),
                "break_even_point": round(break_even_point, 2)
            }
        })
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 400

@app.route("/api/advisor", methods=["POST"])
def ai_advisor():
    """
    Proxies requests securely to Gemini AI
    """
    data = request.json or {}
    prompt = data.get("prompt", "")
    
    if not GEMINI_API_KEY:
        return jsonify({
            "success": False, 
            "error": "Gemini API key is not configured on the server."
        }), 500

    payload = {
        "contents": [{"parts": [{"text": prompt}]}],
        "systemInstruction": {
            "parts": [{"text": "Ты — ИИ-советник приложения CoreMaX AI. Отвечай кратко, ёмко и по делу."}]
        }
    }

    try:
        response = requests.post(
            f"{GEMINI_API_URL}?key={GEMINI_API_KEY}",
            json=payload,
            headers={"Content-Type": "application/json"}
        )
        if response.status_code == 200:
            result = response.json()
            text = result["candidates"][0]["content"]["parts"][0]["text"]
            return jsonify({"success": True, "answer": text})
        else:
            return jsonify({"success": False, "error": response.text}), response.status_code
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    app.run(host="0.0.0.0", port=port, debug=True)
