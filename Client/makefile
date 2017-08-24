
CC = g++
CFLAGS  = -g -Wall
LFLAGS  = -L/usr/lib -lboost_system -lboost_locale -lboost_thread 

all: compiler

compiler: bin/client.o bin/connectionHandler.o
	$(CC) -o bin/client bin/client.o bin/connectionHandler.o $(LFLAGS)
	
bin/client.o:
	$(CC) $(CFLAGS) -c -Linclude -o bin/client.o src/client.cpp
	
bin/connectionHandler.o:
	$(CC) $(CFLAGS) -c -Linclude -o bin/connectionHandler.o src/connectionHandler.cpp
	
clean:
	rm -f bin/*

