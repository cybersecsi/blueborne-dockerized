# BlueBorne Dockerized

This is the repo to reproduce the BlueBorne kill-chain on [Dockerized Android](https://github.com/cybersecsi/dockerized-android) as described [here](https://secsi.it/lab/blueborne-kill-chain-on-dockerized-android), to fully understand the code you should also take a look [here](https://secsi.it/lab/bypass-aslr-through-function-address-inference/)

## Full reproduction
Here are some useful tips for the **full** reproduction of the exploit:
- Create your own FakeSpotify app using the source code in the *webserver* folder (you should use Firebase in order to avoid rebuilding the app when the IP changes)
- Edit your Gmail account (or create a new one) to enable access to less secure apps (to use it on Gophish)
- Run **dependencies.sh** in both *attacker-blueborne* and *attacker-webserver*
- Use ngrok (installed through **dependencies.sh**) to obtain public accessible addresses

