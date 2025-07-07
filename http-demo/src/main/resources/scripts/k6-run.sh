#!/bin/bash

SCRIPT=$1
URL=$2
PAYLOAD=$3
HEADERS=$4
DURATION=$5
VUS=$6
MAX_VUS=$7
QPS_LIST=$8
OUTDIR=$9

#echo "SCRIPT: $SCRIPT"
echo "URL: $URL"
echo "PAYLOAD: $PAYLOAD"
echo "HEADERS: $HEADERS"
echo "DURATION: $DURATION"
echo "VUS: $VUS"
echo "MAX_VUS: $MAX_VUS"
echo "QPS_LIST: $QPS_LIST"
echo "OUTDIR: $OUTDIR"

if [ -n "$OUTDIR" ]; then
    mkdir -p "$OUTDIR"
fi

# 判断是否指定了 OUTDIR
if [ -n "$OUTDIR" ]; then
    OUTFILE="${OUTDIR}/qps_${QPS}.json"
    SUMMARY_ARG="--summary-export=$OUTFILE"
else
    SUMMARY_ARG=""
fi

echo "开始压测所有 QPS 配置..."

for QPS in ${QPS_LIST[@]}; do
    echo "--------------------------------------"
    echo "开始压测：QPS = $QPS"

    k6 run "$SCRIPT" \
        --env TARGET_URL="$URL" \
        --env PAYLOAD="$PAYLOAD" \
        --env HEADERS="$HEADERS" \
        --env QPS="$QPS" \
        --env DURATION="$DURATION" \
        --env VUS="$VUS" \
        --env MAX_VUS="$MAX_VUS" \
        $SUMMARY_ARG
    echo
done

echo "所有压测完成!"
