#!/bin/bash

CP=$(cat classpath.txt)

CORPORA=("corpus.txt" "corpus10.txt" "corpus25.txt")
#CORPORA=("corpus.txt")
WORKERS=(2 4)

WAIT_AFTER_PRODUCER=60

mkdir -p results

for corpus in "${CORPORA[@]}"; do
  for nworkers in "${WORKERS[@]}"; do
    echo "======================================"
    echo "Эксперимент: corpus=$corpus, workers=$nworkers"
    echo "======================================"

    nohup java -cp "$CP" aggregator.AggregatorMain "$nworkers" \
      > "aggregator_${corpus}_w${nworkers}.log" 2>&1 &
    AGG_PID=$!
    echo "Started Aggregator (PID $AGG_PID)"
    sleep 2

    declare -a W_PIDS=()
    for ((i=1; i<=nworkers; i++)); do
      LOG_FILE="worker${i}_${corpus}_w${nworkers}.log"
      nohup java -cp "$CP" worker.WorkerMain > "$LOG_FILE" 2>&1 &
      pid=$!
      W_PIDS+=("$pid")
      echo "Started Worker $i (PID $pid)"
      sleep 1
    done

    sleep 2

    echo "Starting Producer for $corpus..."
    java -cp "$CP" producer.ProducerMain "$corpus"
    echo "Producer finished for $corpus"

    echo "Waiting ${WAIT_AFTER_PRODUCER}s for workers/aggregator to finish..."
    sleep "$WAIT_AFTER_PRODUCER"

    RESULT_FILE=$(ls -t result-*.json 2>/dev/null | head -n 1 || true)
    if [ -n "${RESULT_FILE:-}" ]; then
      TARGET="results/result_${corpus}_w${nworkers}.json"
      cp "$RESULT_FILE" "$TARGET"
      echo "Copied $RESULT_FILE -> $TARGET"
    else
      echo "WARNING: no result-*.json found after experiment"
    fi

    echo "Stopping workers and aggregator..."
    for pid in "${W_PIDS[@]}"; do
      kill "$pid" 2>/dev/null || true
    done
    kill "$AGG_PID" 2>/dev/null || true
    sleep 3

  done
done

echo "All experiments finished."
