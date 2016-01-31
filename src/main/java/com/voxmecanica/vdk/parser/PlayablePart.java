package com.voxmecanica.vdk.parser;

import java.net.URI;

public class PlayablePart {
    private URI src;
    public PlayablePart(){}
    public PlayablePart(URI resource){
        src = resource;
    }

    public URI getSrc(){
        return src;
    }

    public void setSrc(URI src){
        this.src = src;
    }
}
