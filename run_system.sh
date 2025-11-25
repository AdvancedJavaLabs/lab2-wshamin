#!/bin/bash
CP=$(cat classpath.txt)
nohup java -cp $CP aggregator.AggregatorMain > aggregator.log 2>&1 &
AGG_PID=$!
echo "Started Aggregator (PID $AGG_PID)"
sleep 2

nohup java -cp $CP worker.WorkerMain > worker1.log 2>&1 &
W1_PID=$!
echo "Started Worker 1 (PID $W1_PID)"

nohup java -cp $CP worker.WorkerMain > worker2.log 2>&1 &
W2_PID=$!
echo "Started Worker 2 (PID $W2_PID)"

sleep 2
echo "Starting Producer..."
java -cp $CP producer.ProducerMain corpus.txt
echo "Producer finished"
sleep 100
