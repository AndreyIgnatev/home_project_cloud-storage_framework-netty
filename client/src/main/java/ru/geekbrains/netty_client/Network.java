package ru.geekbrains.netty_client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import lombok.extern.slf4j.Slf4j;
import ru.geekbrains.netty_model_obj.AbstractObj;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class Network {
    private  Socket socket;
    private  ObjectEncoderOutputStream out;
    private  ObjectDecoderInputStream in;

     void start() {
        try {
            socket = new Socket("localhost", 8189);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream(), 50 * 1024 * 1024);
        } catch (IOException e) {
            log.error("ERROR new socket, new out, new in ",e);
        }

    }

     void stopClient() {
        try {
            out.close();
        } catch (IOException e) {
            log.error("ERROR out.close()",e);
        }
        try {
            in.close();
        } catch (IOException e) {
            log.error("ERROR in.close()",e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            log.error("ERROR socket.close()",e);
        }
    }



     AbstractObj readObjectMsg()  {
         Object obj = null;
         try {
             obj = in.readObject();
         } catch (ClassNotFoundException e) {
             log.error("Невозможно принять объект!");
         } catch (IOException e) {
             log.error("Невозможно принять объект!");
         }
         return (AbstractObj) obj;
    }

    void writeObjectMsg(AbstractObj obj)  {
        try {
            out.writeObject(obj);
            out.flush();
        } catch (IOException e) {
            log.error("Невозможно отправить объект!");
        }

    }

}