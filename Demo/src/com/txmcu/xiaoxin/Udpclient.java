package com.txmcu.xiaoxin;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.txmcu.xiaoxin.XinStateManager.ConfigType;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
//import android.widget.Toast;

public class Udpclient {
	
	public static interface UdpclientOperations {

		/**
		 * @param init callback ,close wait dialog
		 */
		public void setState(boolean result,String exception);

		
	}
	
	
	public UdpclientOperations operations;
	//public Context contentView;
	private static String TAG = "Udpclient";
	public byte[] send_msg = new byte[100];
	 private AsyncTask<Void, Void, Void> async_cient;
    
    public String recvingMsg;
    DatagramSocket ds = null;
    InetAddress receiverAddress = null;
    int stateCode = 0;
    public void setSendWifiInfo(String ssid,String pwd,String auth_mode,String encryp_type,
    		String channel,String sn,String userid)
    {
    	send_msg =  new byte[140];
    	byte[] bytes =ssid.getBytes();
    	System.arraycopy(bytes,0,send_msg,0,bytes.length);
    	bytes =pwd.getBytes();
    	System.arraycopy(bytes,0,send_msg,20,bytes.length);
    	bytes =auth_mode.getBytes();
    	System.arraycopy(bytes,0,send_msg,40,bytes.length);
    	bytes =encryp_type.getBytes();
    	System.arraycopy(bytes,0,send_msg,60,bytes.length);
    	bytes =channel.getBytes();
    	System.arraycopy(bytes,0,send_msg,80,bytes.length);
    	bytes =sn.getBytes();
    	System.arraycopy(bytes,0,send_msg,100,bytes.length);
    	bytes =userid.getBytes();
    	System.arraycopy(bytes,0,send_msg,120,bytes.length);
    	recvingMsg = "";
    	setStopLoop(0,"");
    	
    	
    	
    }
    private void setStopLoop(int errorcode,String excpetion)
    {
    	stateCode = errorcode;
    	String log = "errorcode:"+errorcode+":"+recvingMsg+":"+excpetion;
    	Log.d(TAG,log);
    	if (stateCode == 2) {
    		operations.setState(true,excpetion);
		}
    	else if (stateCode<0) {
			operations.setState(false,excpetion);
	    	if(async_cient!=null)
	    		async_cient.cancel(false);
		} 
    	//Toast.makeText(getapp, text, duration)
    	//Toast.makeText(contentView, log	, Toast.LENGTH_LONG).show();
    }
    int icount =0;
    @SuppressLint("NewApi")
    public void Looper()
    {
    	if(async_cient!=null)
    		async_cient.cancel(false);

    	Log.d(TAG,"loopcount:"+icount++);
        async_cient = new AsyncTask<Void, Void, Void>() 
        {
            @Override
            protected Void doInBackground(Void... params)
            {  
            	try 
	            {
                	receiverAddress = InetAddress.getByName("192.168.3.1");
                    ds = new DatagramSocket();
	            }
            	catch (SocketException e) 
                {
            		setStopLoop(-1,e.toString());
                } catch (UnknownHostException e) {
					// TODO Auto-generated catch block
                	setStopLoop(-1,e.toString());
				}
            	while(stateCode==0)
            	{
            		sendMsg();

            		//receMsg();
            		if(recvingMsg.startsWith("receive"))
            		{
            			setStopLoop(1,"");
            		}

            		try {
						Thread.sleep(2000);
						Log.d(TAG,"sleep2000 counter:"+icount);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
						//setStopLoop(-2,e.toString());
					}
            	}
            	while(stateCode==1)
            	{
            		receMsg();
            		if(recvingMsg.startsWith("Ok")
            		   ||recvingMsg.startsWith("Fail"))
            		{
            			setStopLoop(2,recvingMsg);
            		}
            		try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
						//setStopLoop(-5,e.toString());
					}
            	}
            	
            
               return null;
            }
            private void receMsg(){
            	 try 
	            {
	            	byte[] receiveData = new byte[20];
	            	 DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	            	 ds.receive(receivePacket);
	            	 recvingMsg = new String( receivePacket.getData());
	            } 
                catch (SocketException e) 
                {
                    e.printStackTrace();
                    setStopLoop(-3,e.toString());
                } catch (IOException e) {
					// TODO Auto-generated catch block
                	 setStopLoop(-3,e.toString());
				}
            }

			private void sendMsg()  {

                try 
                {
                    DatagramPacket dp;                          
                    dp = new DatagramPacket(send_msg, send_msg.length,
                    		receiverAddress, 8888);
                    ds.setBroadcast(true);
                    ds.send(dp);
                } 
                catch (SocketException e) 
                {
                    //e.printStackTrace();
                    setStopLoop(-4,e.toString());
                }
                catch (IOException e) {
                	setStopLoop(-4,e.toString());
				}
               
                
			}

            protected void onPostExecute(Void result) 
            {
               super.onPostExecute(result);
            }
        };

        if (Build.VERSION.SDK_INT >= 11) 
        	async_cient.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else 
        	async_cient.execute();
    }
}
