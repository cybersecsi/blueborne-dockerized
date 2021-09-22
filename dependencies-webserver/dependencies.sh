apt update
apt install -y curl python3 unzip 
curl https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-linux-amd64.zip --output /home/ngrok.zip
unzip /home/ngrok.zip -d /home/
rm /home/ngrok.zip
