app_token="tok_uvppefobpnfqs257jfjkoza4ry"
gitlab_token='$gitlab_token'
gitlab_username="alexkekiy"
curl -s https://$app_token@api.appetize.io/v1/apps/ -F "file=@./app/build/outputs/apk/debug/app-debug.apk" -F "platform=android" > response.json
sudo apt install jq
key=`jq '.publicKey' response.json`
curl --request POST --header "PRIVATE-TOKEN: $gitlab_token" "https://gitlab.com/api/v4/projects/$gitlab_username%2FCarbonFootprint-Mobile/variables" --form "key=APPETIZE_API" --form "value=$app_token" --form "protected=true" > a.txt
curl --request POST --header "PRIVATE-TOKEN: $gitlab_token" "https://gitlab.com/api/v4/projects/$gitlab_username%2FCarbonFootprint-Mobile/variables" --form "key=APPETIZE_KEY" --form "value=$key" --form "protected=true" > a.txt
rm a.txt
echo
echo `jq '.appURL' response.json`
