import sys

from pyspark import SparkContext
from pyspark.streaming import StreamingContext
import requests

def sendData(time, rdd):
    print("-------------------------------------------")
    print("Time: %s" % time)
    print("-------------------------------------------")
    line = rdd.collect()
    #print(line)
    url = 'http://0.0.0.0:4444/update'
    response = requests.post(url, json={'chave': line})
    print(response)

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: stateful_network_wordcount.py <hostname> <port>", file=sys.stderr)
        sys.exit(-1)
    sc = SparkContext(appName="PythonStreamingStatefulNetworkWordCount")
    ssc = StreamingContext(sc, 1)
    ssc.checkpoint("checkpoint")

    # RDD with initial state (key, value) pairs
    initialStateRDD = sc.parallelize([(u'hello', 1), (u'world', 1)])

    def updateFunc(new_values, last_sum):
        return sum(new_values) + (last_sum or 0)

    lines = ssc.socketTextStream(sys.argv[1], int(sys.argv[2]))
    running_counts = lines.flatMap(lambda line: line.split(" "))\
                          .map(lambda word: (word, 1))\
                          .updateStateByKey(updateFunc, initialRDD=initialStateRDD)

    running_counts.foreachRDD(sendData)

    ssc.start()
    ssc.awaitTermination()