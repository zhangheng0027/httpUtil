package com.zh.httpProxy;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class HttpTunnelClientTest {

    @Test
    public void runC() throws IOException {
        HttpTunnelClient htc = new HttpTunnelClient(1234, "127.0.0.1", 12345);
        htc.run();
    }

}