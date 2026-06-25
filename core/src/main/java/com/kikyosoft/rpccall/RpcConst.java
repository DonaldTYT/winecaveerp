package com.kikyosoft.rpccall;


interface RpcConst {
  static final int SOH = 0x01;
  static final int STX = 0x02;
  static final int ETX = 0x03;
  static final int EOT = 0x04;
  static final int ENQ = 0x05;
  static final int ACK = 0x06;
  static final int DC1 = 0x11;
  static final int DC2 = 0x12;
  static final int DC3 = 0x13;
  static final int DC4 = 0x14;
  static final int NAK = 0x15;
  static final int SYN = 0x16;
  static final int ETB = 0x17;
  static final int CAN = 0x18;
  static final  char RPCCALLSEGMENTID = 'A';
  static final char RPCCALLFUNCTIONID = 'B';
  static final char FILECALLSEGMENTID = 'C';
  static final char FILECALLFUNCTIONID = 'D';
  static final char FILECALLFILEID = 'E';
  static final char RPCPUTFILEID = 'F';
  static final char RPCGETFILEID = 'G';
  static final char RPCGETFILEID2 = 'g';
  static final char RPCGETFILEIDWITHTIME = 'H';
  static final  char RPCCALLSEGMENTID_NORETURN = 'J';
  static final  char RPCCALLSEGMENTID_NOACK = 'K';
  static final int RPCPUTLEN = 4096;
  static final int RPCACKTIMEOUT = 30;
  static final int RPCPUTFILETIMEOUT = 60;
}
