

# Project Setup

## 1. Get the Code

```bash
git clone (https://github.com/mars216/TCMMFK.git)
cd TCMMFK
git lfs pull

## 2. Import Data

#### 2.1 Import Data
CREATE DATABASE honey2024 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
mysql -u root -p honey2024 < data/honey2024.sql

#### 2.1 Neo4j Import（Neo4j 5.x）
neo4j-admin database load --from=data/neo4j.dump neo4j --overwrite-destination=true

## 3.Start Spring Boot
cd springBoot
mvn clean install
mvn spring-boot:run

##4. Start Flask
cd ../flask
conda create -n flask-env python=3.9 -y
conda activate flask-env
pip install -r requirements.txt   # or manually install flask, pymysql, neo4j, etc.
python app.py

##5. Start Vue
cd ../vue
npm install
npm run serve
