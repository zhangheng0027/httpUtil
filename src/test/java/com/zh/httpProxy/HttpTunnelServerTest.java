package com.zh.httpProxy;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class HttpTunnelServerTest {

    @Test
    public void startS() throws IOException {

        HttpTunnelServer hts = new HttpTunnelServer(12345);
        hts.run();

    }

}