#include <stdlib.h>
#include <boost/locale.hpp>
#include <boost/thread.hpp>
#include "connectionHandler.h"
#include <string>

void inputThread(ConnectionHandler* connectionHandler);
bool isEnd;

int main (int argc, char *argv[]) {
   // if (argc < 3) {
   //     std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
    //    return -1;
  //  }
 //   std::string host = argv[1];
   // short port = atoi(argv[2]);
	std::string host ="127.0.0.1";
	short port =8090;
    isEnd=false;
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    boost::thread readerThread(inputThread, &connectionHandler);

	std::cout << "client ready\n";

    while (!isEnd) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
		std::string line(buf);
        if (!connectionHandler.sendLine(line)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
    }
    return 0;
}

void inputThread(ConnectionHandler* connectionHandler){
	while(!isEnd){
		std::string answer;
		if (!connectionHandler->getLine(answer)) {
			std::cout << "Disconnected. Exiting...\n" << std::endl;
		}
		int len=answer.length();
		answer.resize(len-1);
		std::cout << answer << std::endl;
		if (answer == "SYSMSG QUIT ACCEPTED") {
			std::cout << "Exiting...\nPress ENTER to terminate" << std::endl;
			isEnd=true;
		}
	}
}
