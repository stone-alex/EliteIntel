# Installing Local Text To Speech

## Piper
Piper is A fast and local neural text-to-speech engine that embeds espeak-ng for phonemization.
[Piper TTS project](https://github.com/OHF-Voice/piper1-gpl)

### 🐧 Linux Install

```shell
## replace with whatever directory you want
sudo mkdir -p /path/to/piper-tts
cd /path/to/piper-tts

## install piper-tts
python3 -m venv .
source bin/activate
pip install --upgrade pip
pip install piper-tts[http]

## download voice models
## Amy
wget -O en_US-amy-medium.onnx https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/amy/medium/en_US-amy-medium.onnx
wget -O en_US-amy-medium.onnx.json https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/amy/medium/en_US-amy-medium.onnx.json

## Joe
wget -O en_US-joe-medium.onnx https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/joe/medium/en_US-joe-medium.onnx
wget -O en_US-joe-medium.onnx.json https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/joe/medium/en_US-joe-medium.onnx.json
```

Run Piper local Web server (still inside the virtual env)
```shell
python3 -m piper.http_server -m en_US-amy-medium.onnx
```

The server listens on port 5000.

To test, run
```shell
curl -X POST -H 'Content-Type: application/json' -d '{ "text": "This is a test." }' -o test.wav localhost:5000
aplay test.wav 
```

NOTE: The in-app voice switch will not work with Piper.

### To use in the app
Simply do not provide TTS key. The app will use Piper TTS as default.
