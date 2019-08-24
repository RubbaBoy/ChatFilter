/*
 * ChatFilter
 * Copyright (C) 2019 Craftathon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
