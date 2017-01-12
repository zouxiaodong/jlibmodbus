package com.invertor.modbus.serial;

import com.invertor.modbus.Modbus;
import com.invertor.modbus.net.stream.InputStreamTCP;
import com.invertor.modbus.net.stream.OutputStreamTCP;
import com.invertor.modbus.tcp.TcpParameters;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/*
 * Copyright (C) 2017 Vladislav Y. Kochedykov
 *
 * [http://jlibmodbus.sourceforge.net]
 *
 * This file is part of JLibModbus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors: Vladislav Y. Kochedykov, software engineer.
 * email: vladislav.kochedykov@gmail.com
 */

public class SerialPortFactoryTcp implements SerialPortAbstractFactory {

    private TcpParameters tcpParameters;

    public SerialPortFactoryTcp(TcpParameters tcpParameters) {
        setTcpParameters(tcpParameters);
    }

    public TcpParameters getTcpParameters() {
        return tcpParameters;
    }

    public void setTcpParameters(TcpParameters tcpParameters) {
        this.tcpParameters = tcpParameters;
    }

    public SerialPort createSerial(SerialParameters sp) throws SerialPortException {
        return new SerialPortViaTCP(sp);
    }

    private class SerialPortViaTCP extends SerialPort {

        private Socket socket;
        private InputStreamTCP in;
        private OutputStreamTCP os;

        public SerialPortViaTCP(SerialParameters sp) throws SerialPortException {
            super(sp);
        }

        @Override
        public void purgeRx() {
            //do nothing
        }

        @Override
        public void purgeTx() {
            //do nothing
        }

        @Override
        public void open() throws SerialPortException {
            TcpParameters parameters = getTcpParameters();
            if (parameters != null) {
                close();
                socket = new Socket();
                InetSocketAddress isa = new InetSocketAddress(parameters.getHost(), parameters.getPort());
                try {
                    socket.connect(isa, Modbus.MAX_CONNECTION_TIMEOUT);
                    socket.setKeepAlive(parameters.isKeepAlive());

                    socket.setSoTimeout(Modbus.MAX_RESPONSE_TIMEOUT);

                    in = new InputStreamTCP(socket);
                    os = new OutputStreamTCP(socket);
                } catch (Exception e) {
                    throw new SerialPortException(e);
                }
            }
        }

        @Override
        public void write(byte[] b) throws IOException {
            if (isOpened()) {
                os.write(b);
                os.flush();
            } else {
                throw new IOException("Port not opened");
            }
        }

        @Override
        public void write(int b) throws IOException {
            if (isOpened()) {
                os.write(b);
                os.flush();
            } else {
                throw new IOException("Port not opened");
            }
        }

        @Override
        public int read() throws IOException {
            if (isOpened()) {
                return in.read();
            } else {
                throw new IOException("Port not opened");
            }
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (isOpened()) {
                return in.read(b, off, len);
            } else {
                throw new IOException("Port not opened");
            }
        }

        @Override
        public void close() {
            try {
                if (socket != null) {
                    socket.close();
                }
                if (in != null) {
                    in.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                //do nothing
            } finally {
                socket = null;
                os = null;
                in = null;
            }
        }

        public void setReadTimeout(int readTimeout) {
            super.setReadTimeout(readTimeout);
            try {
                socket.setSoTimeout(readTimeout);
            } catch (Exception e) {
                //do nothing
            }
        }

        @Override
        public boolean isOpened() {
            return socket != null && socket.isConnected();
        }
    }
}