package com.craftathon.chatfilter3.bench;

import com.craftathon.chatfilter3.utils.SwearIpsum;
import org.craftathon.chatfilter3.main.ChatFilter;
import org.craftathon.chatfilter3.main.DefaultChatFilter;
import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
public class SingleFilterPlan {

    public String longLine = "biᾰὰáṱṯcch ante ipsum bbbo¤ḡ horny chhỗợoḋȅ pu➎şi ẉwaặﬡk hoⓡnie ultrices sἤἥἤὴiｔ ｍủuứṻfｆ Curae; Aenean neὲgｇrổ wᾧaaἅanｎk @ậ➍s５５ lube lacinia fửccck tincidunt vitae";
    public String shortLine = "coccc¢k cṻnt ultrices nibh";
    public ChatFilter chatFilter;

    @Setup(Level.Trial)
    public void setUp() {
        chatFilter = new DefaultChatFilter();
        chatFilter.init();
    }
}
