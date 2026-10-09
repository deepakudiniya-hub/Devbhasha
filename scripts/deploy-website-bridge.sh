#!/bin/bash
# One-shot deploy of the devbhasha.com <-> Dev Panel bridge (publicSadhaks, websiteEnquiry + websiteEnquiries rules).
# Run in Google Cloud Shell:  curl -sL https://raw.githubusercontent.com/deepakudiniya-hub/Devbhasha/hark/website-devpanel/scripts/deploy-website-bridge.sh | bash
P=devbhasha-d9e22
gcloud config set project $P -q >/dev/null 2>&1
rm -rf ~/dbbridge && git clone -q --depth 1 -b hark/website-devpanel https://github.com/deepakudiniya-hub/Devbhasha.git ~/dbbridge && cd ~/dbbridge || { echo "CLONE FAILED"; exit 1; }
(cd functions && npm install --no-audit --no-fund >/dev/null 2>&1)
echo "== Deploying functions (2-4 min) =="
for F in publicSadhaks websiteEnquiry; do
  gcloud functions deploy $F --gen2 --runtime=nodejs22 --region=us-central1 --source=functions --entry-point=$F \
    --trigger-http --allow-unauthenticated --set-env-vars=FIRESTORE_DB_ID=default,AGORA_APP_ID=$(grep AGORA_APP_ID functions/.env | cut -d= -f2) \
    --max-instances=5 --quiet >/tmp/$F.log 2>&1 && echo "OK $F" || { echo "FAIL $F"; tail -5 /tmp/$F.log; }
done
echo "== Publishing Firestore rules (database: default) =="
T=$(gcloud auth print-access-token)
RS=$(python3 -c 'import json;print(json.dumps({"source":{"files":[{"name":"firestore.rules","content":open("firestore.rules").read()}]}}))' | \
  curl -s -X POST -H "Authorization: Bearer $T" -H "Content-Type: application/json" -H "x-goog-user-project: $P" \
  "https://firebaserules.googleapis.com/v1/projects/$P/rulesets" -d @- | python3 -c 'import sys,json;print(json.load(sys.stdin).get("name",""))')
if [ -n "$RS" ]; then
  curl -s -X PATCH -H "Authorization: Bearer $T" -H "Content-Type: application/json" -H "x-goog-user-project: $P" \
    "https://firebaserules.googleapis.com/v1/projects/$P/releases/cloud.firestore/default" \
    -d "{\"release\":{\"name\":\"projects/$P/releases/cloud.firestore/default\",\"rulesetName\":\"$RS\"}}" | grep -q rulesetName && echo "OK rules" || echo "FAIL rules"
else echo "FAIL rules (ruleset)"; fi
echo "== Test =="
curl -s "https://us-central1-$P.cloudfunctions.net/publicSadhaks" | head -c 300; echo
echo "== ALL DONE =="
