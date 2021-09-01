from pyignite import Client
from threading import Thread
import random


#establish connection
client = Client()
client.connect('34.72.162.100', 10800)

client_1 = client.get_cache("client_1")


def read_file(file_name):
    with open(file_name,'r',encoding="utf8") as f:
        for line in f:
            for word in line.split():
                try:

                    client_1.put(word,5)
                    print("Adicionando " + word)
                except:
                    print(word + " ja existe")


def search_word(file_name):
    with open(file_name,'r',encoding="utf8") as f:
        for line in f:
            for word in line.split():
                try:
                    client_1.get(word)
                    print("******************")
                    print("Achou " + word)
                    print("******************")
                except:
                    print("Nao achou " + word)
                
                

                
reader_one = Thread(target=read_file,args=['txt-biblia.txt'])
reader_two = Thread(target=read_file,args=['txt-hp1.txt'])
reader_three = Thread(target=read_file,args=['txt-hp2.txt'])
reader_four = Thread(target=read_file,args=['txt-hp3.txt'])
reader_five = Thread(target=read_file,args=['txt-hp4.txt'])
reader_six = Thread(target=read_file,args=['txt-hp2.txt'])
reader_seven = Thread(target=read_file,args=['txt-hp3.txt'])
reader_eight = Thread(target=read_file,args=['txt-hp4.txt'])
reader_nine = Thread(target=read_file,args=['txt-hp2.txt'])
reader_10 = Thread(target=read_file,args=['txt-hp3.txt'])
reader_11 = Thread(target=read_file,args=['txt-hp4.txt'])
reader_12 = Thread(target=read_file,args=['txt-hp2.txt'])
reader_13 = Thread(target=read_file,args=['txt-hp3.txt'])
reader_14 = Thread(target=read_file,args=['txt-hp4.txt'])

searcher_one = Thread(target=search_word,args=['txt-hp1.txt'])
searcher_two = Thread(target=search_word,args=['txt-hp2.txt'])
searcher_three = Thread(target=search_word,args=['txt-hp3.txt'])
searcher_four = Thread(target=search_word,args=['txt-hp4.txt'])


reader_one.start()
reader_two.start()
reader_three.start()
reader_four.start()
reader_five.start()
reader_six.start
reader_seven.start()
reader_eight.start()
reader_nine.start
reader_10.start()
reader_11.start()
reader_12.start
reader_13.start()
reader_14.start()

searcher_one.start()
searcher_two.start()
searcher_three.start()
searcher_four.start()

