/**
 * @file Outlines.java
 * @date 27 May 2016
 */

package uk.ac.warwick.wsbc.QuimP;

import java.util.ArrayList;

/**
 * Represent collection of OutlineHandlers
 * 
 * @author p.baniukiewicz
 * @date 27 May 2016
 *
 */
public class Outlines implements IQuimpSerialize {

    public ArrayList<OutlineHandler> oHs;

    public Outlines(int size) {
        oHs = new ArrayList<>(size);
    }

    public Outlines() {
        oHs = new ArrayList<>();
    }

    @Override
    public void beforeSerialize() {

    }

    @Override
    public void afterSerialize() throws Exception {

    }
}
