package com.uniinformation.rpi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;


public class FBIUtil {
	/***
	 * changes log:
	 * 1.10 - version for initial demo
	 * 1.11 - fix button cannot display config by add oLock
	 * 1.12 - match physical led color to logical led color name correctly
	 * 1.13 - add delay after showconfig
	 * 1.14 - support 8 pic
	 */
	static String VERSION_TAG = "1.14";
	

	//GPIO control using pi4j , document refer to the following url
    //https://pi4j.com/1.2/example/control.html
	//Warning !!! the following gpio number is according to WiringPi, it is not equals to Broadcomm GPIO number.
	//refer to the above url for the location of each pin
	//
	//	static final Pin ledGpio[] = new Pin[]{
	//			RaspiPin.GPIO_22,
	//			RaspiPin.GPIO_23,
	//			RaspiPin.GPIO_24,
	//			RaspiPin.GPIO_25
	//	};
	//	static final Pin swGpio[] = new Pin[]{
	//			RaspiPin.GPIO_26,
	//			RaspiPin.GPIO_27,
	//			RaspiPin.GPIO_28,
	//			RaspiPin.GPIO_29
	//	};

    static GpioController gpio;
	static GpioPinDigitalOutput[] led;
	static GpioPinDigitalInput[] sw;
	static int buttonMask = 0;


	static int IMAGE_COUNT;
	static int currentImg = 0;
	
	
	// sample script:start.sh
	//	#!/bin/bash
	//	##########################################
	//	#you can add this script to /home/pi/.bashrc
	//	#sudo bash -c /usr/app/fbiutil/start.sh
	//	##########################################
	//	check_running(){
	//	   flock -n 200
	//	   if [ $? -ne 0 ]; then
	//	      echo "app is running, force abort" >&2
	//	      exit 1
	//	   fi
	//	}
	//	(
	//	date
	//	check_running
	//
	//	stty raw -echo
	//	setterm -foreground black -cursor off
	//	cd /usr/app/fbiutil
	//	java com.uniinformation.rpi.FBIUtil
	//	killall -15 fbi
	//	reset
	//	) 200>/usr/app/fbiutil/fbiutil.lock
	
	// sample script:showconfig.sh
	//	#!/bin/bash
	//	clear
	//	echo ""
	//	echo "Network Config:"
	//	echo "================"
	//	/usr/sbin/ifconfig -a
	//	IP=`/usr/sbin/ifconfig |grep "inet " |grep -v 127.0.0 |sed -e "s/^.*inet \([0-9.]*\).*/\1/"`
	//
	//	if [ "x" == "x$IP" ]; then
	//	   IP="IPADDR"
	//	fi
	//	echo "Upload image URL:"
	//	echo "================="
	//	echo "http://$IP"
	//	echo ""


	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
	public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
	public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
	public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
	public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
	public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
	public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
	public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
	public static AtomicBoolean screenDirty = new AtomicBoolean(true);
	public static Object oLock = new Object();

	public static void showImg(String p_img) {
		synchronized(oLock) {
			try {
				clearScreen();

				//call fbi
				Process p = Runtime.getRuntime().exec(new String[] {"/bin/bash", "-c", String.format("(killall -15 fbi; /usr/bin/fbi --noverbose --nocomments --noedit %s -T 1 -d /dev/fb0) >/tmp/fbi.log 2>&1 </dev/null", p_img)});
				p.waitFor();
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	public static void clearScreen() {
		if (!screenDirty.get()) return;
		System.out.print("\033[H\033[2J");   
		System.out.flush();   
	}
	public static void showConfig() {
		synchronized(oLock) {
			try {
				//kill old fbi
				Process p = Runtime.getRuntime().exec(new String[] {"/bin/bash", "-c", "killall -15 fbi 2>/dev/null;" });
				p.waitFor();
				clearScreen();

				//read network config
				p = Runtime.getRuntime().exec(new String[] {"/bin/bash", "-c", "/usr/app/fbiutil/showconfig.sh" });
				p.waitFor();
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String s = null;
				while ((s = stdInput.readLine()) != null) {
					log(s);
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	public static void log(String p_log) {
		screenDirty.set(true);
		System.out.print(ANSI_WHITE+p_log+"\r\n"+ANSI_BLACK);
	}
	public static void cmdline() {
		clearScreen();
		//showConfig(); //ip is not ready if launched by auto start
		
		log("\n\nPlease input option (version "+VERSION_TAG+") : [1,2,3,4,5,6,7,8,9,q]: ");
		for (;;) {
			try {
				int key = System.in.read();  //read key (require to enable stty raw)
				if (key == 113  || key == 81 || key == 3) { //q,Q,ctrl+c
					break;
				}
				//System.out.println(String.format("key:%d", key));
				switch((key-48)) {
				case 1: 
					//showImg("/usr/share/rpd-wallpaper/cliff.jpg");
					showImg("/usr/app/fbiutil/images/1.jpg");
					currentImg = 1;
					updateLed();
					break;
				case 2:
					//showImg("/usr/share/rpd-wallpaper/bridge.jpg");
					showImg("/usr/app/fbiutil/images/2.jpg");
					currentImg = 2;
					updateLed();
					break;
				case 3:
					//showImg("/usr/share/rpd-wallpaper/trees.jpg");
					showImg("/usr/app/fbiutil/images/3.jpg");
					currentImg = 3;
					updateLed();
					break;
				case 4:
					showImg("/usr/app/fbiutil/images/4.jpg");
					currentImg = 4;
					updateLed();
					break;
				case 5:
					showImg("/usr/app/fbiutil/images/5.jpg");
					currentImg = 5;
					updateLed();
					break;
				case 6:
					showImg("/usr/app/fbiutil/images/6.jpg");
					currentImg = 6;
					updateLed();
					break;
				case 7:
					showImg("/usr/app/fbiutil/images/7.jpg");
					currentImg = 7;
					updateLed();
					break;
				case 8:
					showImg("/usr/app/fbiutil/images/8.jpg");
					currentImg = 8;
					updateLed();
					break;
				case 9:
					showConfig();
					currentImg = 0;
					updateLed();
					Thread.sleep(1000);
					break;
				default:
					log(String.format("key not supported:%d", key));
					updateLed();
					break;
				
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	static class myGpioPinListenerDigital implements GpioPinListenerDigital {
		int idx;
		myGpioPinListenerDigital(int p_idx) {
			super();
			idx = p_idx;
		}
		@Override
		public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
			// display pin state on console
			// System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
			if(event.getState().isLow()) {
				buttonMask |= (1 << (idx-1));
				if(buttonMask == 0x3){
					// clear buttonMask to avoid two many switch to showconfig;
					// the buttomMask at this stage is not equal to the real button status
					buttonMask = 0; 

					currentImg = 0;
					updateLed();
					showConfig();
				} else {
					if(currentImg != idx) {
						showImg("/usr/app/fbiutil/images/"+idx+".jpg");
						currentImg = idx;
						updateLed();
					}
				}
			} else {
				buttonMask &= ~(1 << (idx-1));
			}
		}
	};
	static void initGpio() {
		gpio = GpioFactory.getInstance();
		IMAGE_COUNT = 8;
		led = new GpioPinDigitalOutput[IMAGE_COUNT];
		led[0] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "LED_green", PinState.LOW);
		led[1] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26, "LED_blue", PinState.LOW);
		led[2] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "LED_yellow", PinState.LOW);
		led[3] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "LED_red", PinState.LOW);
		led[4] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "LED_green", PinState.LOW);
		led[5] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26, "LED_blue", PinState.LOW);
		led[6] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "LED_yellow", PinState.LOW);
		led[7] = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "LED_red", PinState.LOW);
		
		sw = new GpioPinDigitalInput[IMAGE_COUNT];
		sw[0] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, PinPullResistance.PULL_UP);
		sw[1] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23, PinPullResistance.PULL_UP);
		sw[2] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_27, PinPullResistance.PULL_UP);
		sw[3] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_22, PinPullResistance.PULL_UP);
		sw[4] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, PinPullResistance.PULL_UP);
		sw[5] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23, PinPullResistance.PULL_UP);
		sw[6] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_27, PinPullResistance.PULL_UP);
		sw[7] = gpio.provisionDigitalInputPin(RaspiPin.GPIO_22, PinPullResistance.PULL_UP);
		
		sw[0].addListener(new myGpioPinListenerDigital(1));
		sw[1].addListener(new myGpioPinListenerDigital(2));
		sw[2].addListener(new myGpioPinListenerDigital(3));
		sw[3].addListener(new myGpioPinListenerDigital(4));
		sw[4].addListener(new myGpioPinListenerDigital(5));
		sw[5].addListener(new myGpioPinListenerDigital(6));
		sw[6].addListener(new myGpioPinListenerDigital(7));
		sw[7].addListener(new myGpioPinListenerDigital(8));

		/*
		final GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_24, PinPullResistance.PULL_UP);
		myButton.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
            }
        });
		*/

	}
	static void updateLed() {
		for(int i=0;i< IMAGE_COUNT;i++) {
			if((currentImg == 0) || ((currentImg-1)==i)) {
				led[i].high();
			} else {
				led[i].low();
			}
		}
	}
	public static void main(String args[]) {
		initGpio();
		updateLed();
		cmdline();
	}
}
