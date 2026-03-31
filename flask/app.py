
import os
from flask import Flask, request, jsonify
from flask_cors import CORS
from core import (
    validate_file, read_docx_file, chunk_text, extract_kg_from_text,
    Neo4jClient
)
from predict_targets_mlp_interaction_cpu import HerbTargetPredictor

predictor = HerbTargetPredictor()
app = Flask(__name__)
CORS(app, origins="*", supports_credentials=True, methods=["GET", "POST", "OPTIONS"], allow_headers=["*"])
app.config['WTF_CSRF_ENABLED'] = False
app.config['UPLOAD_FOLDER'] = "uploads"
os.makedirs(app.config["UPLOAD_FOLDER"], exist_ok=True)

neo4j_client = Neo4jClient()

@app.route('/api/ping', methods=['GET'])
def ping():
    return jsonify({"code": 200, "msg": "ok"})

@app.route('/api/extract', methods=['POST'])
def api_extract():
    try:
        if "file" not in request.files:
            return jsonify({"code": 400, "msg": "No file selected"}), 400
        file = request.files["file"]
        if file.filename == "":
            return jsonify({"code": 400, "msg": "No file selected"}), 400

        is_valid, msg = validate_file(file)
        if not is_valid:
            return jsonify({"code": 400, "msg": msg}), 400

        file_path = os.path.join(app.config["UPLOAD_FOLDER"], file.filename)
        file.save(file_path)

        text = read_docx_file(file_path)
        if not text:
            return jsonify({"code": 400, "msg": "File content is empty"}), 400

        chunks = chunk_text(text)
        all_triples = []
        all_entities = []
        for chunk in chunks:
            triples, entities = extract_kg_from_text(chunk)
            all_triples.extend(triples)
            all_entities.extend(entities)

        return jsonify({
            "code": 200,
            "msg": "Extraction successful",
            "data": {
                "filename": file.filename,
                "triples": all_triples,
                "entities": all_entities
            }
        }), 200

    except Exception as e:
        return jsonify({"code": 500, "msg": f"Processing failed: {str(e)}"}), 500

@app.route('/api/save', methods=['POST'])
def api_save():
    """Save triples and entity attributes"""
    try:
        data = request.get_json()
        if not data:
            return jsonify({"code": 400, "msg": "Request body is empty"}), 400

        doc_name = data.get("document_name")
        triples = data.get("triples", [])
        entities = data.get("entities", [])

        if not doc_name:
            return jsonify({"code": 400, "msg": "document_name cannot be empty"}), 400

        # 1. Build entity type mapping (for triple saving)
        entity_type_map = {}
        for ent in entities:
            name = ent.get("name")
            etype = ent.get("type")
            if name and etype:
                entity_type_map[name] = etype

        # 2. Save triples (requires type mapping)
        if triples:
            tri_success, tri_msg = neo4j_client.save_triples(doc_name, triples, entity_type_map)
            if not tri_success:
                return jsonify({"code": 500, "msg": f"Failed to save triples: {tri_msg}"}), 500

        # 3. Save entity attributes
        for ent in entities:
            name = ent.get("name")
            etype = ent.get("type")
            attrs = ent.get("attributes", {})
            if name and etype:
                attr_success, attr_msg = neo4j_client.update_entity_attributes(name, etype, attrs)
                if not attr_success:
                    # Optionally log the error, but continue saving other entities
                    print(f"Failed to save attributes for entity {name}: {attr_msg}")

        return jsonify({
            "code": 200,
            "msg": "Save successful (triples and entity attributes written to Neo4j)"
        }), 200

    except Exception as e:
        return jsonify({"code": 500, "msg": f"Save failed: {str(e)}"}), 500

@app.route('/api/predict_targets', methods=['POST'])
def api_predict_targets():
    try:
        data = request.get_json()
        herb_name = data.get('herb_name')
        top_k = data.get('top_k', 50)

        if not herb_name:
            return jsonify({"code": 400, "msg": "herb_name cannot be empty"}), 400

        # 1. Find herb entity ID by name
        herb_node = neo4j_client.find_herb_by_name(herb_name)
        if not herb_node:
            return jsonify({"code": 404, "msg": f"Herb not found: {herb_name}"}), 404
        herb_entity_id = herb_node.get("hit2_id")
        if not herb_entity_id:
            return jsonify({"code": 500, "msg": "Herb is not within prediction scope"}), 500

        # 2. Call predictor
        predictions = predictor.predict(herb_entity_id, top_k=top_k)

        # 3. Enrich with protein details
        results = []
        for prot_idx, score in predictions:
            prot_entity_id = predictor.prot_id2e.get(prot_idx)
            if not prot_entity_id:
                continue
            prot_node = neo4j_client.find_protein_by_entity_id(prot_entity_id)
            if prot_node:
                prot_info = {
                    "entity_id": prot_entity_id,
                    "name": prot_node.get("name", ""),
                    "description": prot_node.get("Description", ""),
                    "uniprot_id": prot_node.get("id", ""),
                    "score": float(score)
                }
            else:
                prot_info = {
                    "entity_id": prot_entity_id,
                    "name": "",
                    "description": "",
                    "score": float(score)
                }
            results.append(prot_info)

        return jsonify({"code": 200, "msg": "Prediction successful", "data": {"predictions": results}}), 200

    except Exception as e:
        return jsonify({"code": 500, "msg": f"Prediction failed: {str(e)}"}), 500

@app.route('/api/herb_info', methods=['GET'])
def api_herb_info():
    try:
        herb_name = request.args.get('name')
        if not herb_name:
            return jsonify({"code": 400, "msg": "Parameter 'name' cannot be empty"}), 400

        # Query herb node from Neo4j
        herb_node = neo4j_client.find_herb_by_name(herb_name)
        if not herb_node:
            return jsonify({"code": 404, "msg": f"Herb not found: {herb_name}"}), 404

        # Extract node attributes (adjust according to actual attribute names)
        herb_info = {
            "entity_id": herb_node.get("hit2_id", ""),
            "name": herb_node.get("herb_cn_name", ""),
            "alias": herb_node.get("herb_en_name", ""),
            "family": herb_node.get("introduction", ""),
            "part": herb_node.get("channel_tropism", ""),
            "taste": herb_node.get("taste", "")
        }
        return jsonify({"code": 200, "msg": "Success", "data": herb_info}), 200

    except Exception as e:
        return jsonify({"code": 500, "msg": f"Query failed: {str(e)}"}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True, threaded=True)
