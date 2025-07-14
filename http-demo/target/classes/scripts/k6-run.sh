#!/bin/bash

SCRIPT=$1
ADDR=$2
REQUESTS=$3
DURATION=$4
VUS=$5
MAX_VUS=$6
QPSs=$7
OUT_DIR=$8

#echo "SCRIPT: $SCRIPT"
echo "ADDR: $ADDR"
echo "REQUESTS: $REQUESTS"
echo "DURATION: $DURATION"
echo "VUS: $VUS"
echo "MAX_VUS: $MAX_VUS"
echo "QPSs: $QPSs"
echo "OUT_DIR: $OUT_DIR"

if [ -n "$OUT_DIR" ]; then
    mkdir -p "$OUT_DIR"
fi

echo "开始压测所有 QPS 配置..."

for QPS in ${QPSs[@]}; do
    echo "--------------------------------------"
    echo "开始压测：QPS = $QPS"

    # 判断是否指定了 $OUT_DIR
    if [ -n "$OUTDIR" ]; then
        OUTFILE="${$OUT_DIR}/qps_${QPS}.json"
        SUMMARY_ARG="--summary-export=$OUTFILE"
    else
        SUMMARY_ARG=""
    fi

    k6 run "$SCRIPT" \
        --env ADDR="$ADDR" \
        --env REQUESTS="$REQUESTS" \
        --env QPS="$QPS" \
        --env DURATION="$DURATION" \
        --env VUS="$VUS" \
        --env MAX_VUS="$MAX_VUS" \
        $SUMMARY_ARG
    echo
done

echo "所有压测完成!"
