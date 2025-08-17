import boto3, json, difflib

s3 = boto3.client("s3")

def _txt(bucket, key):
    return s3.get_object(Bucket=bucket, Key=key)["Body"].read().decode("utf-8", "ignore").splitlines()

def handler(event, _):
    job = event["jobId"]
    inb, outb = event["inputBucket"], event["outputBucket"]
    akey, bkey = event["keyA"], event["keyB"]

    A, B = _txt(inb, akey), _txt(inb, bkey)
    diffs = []
    for tag, i1, i2, j1, j2 in difflib.SequenceMatcher(a=A, b=B, autojunk=False).get_opcodes():
        if tag == "equal": continue
        if tag == "insert":  diffs.append({"type":"add",    "b":"\n".join(B[j1:j2])})
        elif tag == "delete": diffs.append({"type":"remove", "a":"\n".join(A[i1:i2])})
        else:                 diffs.append({"type":"change", "a":"\n".join(A[i1:i2]), "b":"\n".join(B[j1:j2])})

    s3.put_object(
        Bucket=outb, Key=f"{job}/result.json",
        Body=json.dumps({"jobId": job, "status": "COMPLETE", "diffs": diffs}).encode("utf-8"),
        ContentType="application/json",
    )
    return {"ok": True}
