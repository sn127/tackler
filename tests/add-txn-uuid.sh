
txns=$1
grep '^20' "$txns"  | while read line; do echo " ;:uuid: $(uuidgen)"; done >> "$txns" 

