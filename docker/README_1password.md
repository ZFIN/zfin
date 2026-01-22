Usage
=====
Setup your 1Password account on the server (first time only):
_NOTE: You will need your 1Password Secret Key as well as your account password._

op account add

Sign in to your 1Password account using the following command (requires account password):
_NOTE: Only needs to be done once per session._

eval $(op signin)

Create .env file from template:

op inject --in-file environment_linux.op_tmpl --out-file .env
