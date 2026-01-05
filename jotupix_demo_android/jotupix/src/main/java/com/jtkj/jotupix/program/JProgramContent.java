package com.jtkj.jotupix.program;

import com.jtkj.jotupix.utils.JByteWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for multiple content objects.
 * <p>
 * This class manages a collection of JContentBase objects. It provides
 * methods to add content, clear all content, and serialize the entire
 * content list into a byte array.
 */
public class JProgramContent {

    private List<JContentBase> contentData;

    /**
     * Constructor
     */
    public JProgramContent() {
        this.contentData = new ArrayList<>();
    }

    /**
     * Add a content object to the container.
     *
     * @param content A JContentBase object.
     */
    public void add(JContentBase content) {
        if (content != null) {
            contentData.add(content);
        }
    }

    /**
     * Remove all content from the container.
     */
    public void clear() {
        contentData.clear();
    }


    /**
     * Pack content data according to the protocol format.
     * <p>
     * Generate the protocol-compliant byte sequence that represents its content.
     *
     * @return A byte buffer containing the packaged content.
     */
    public byte[] get() {
        JByteWriter content = new JByteWriter();
        // Put 8 bytes of zeros
        content.putRepeatByteOne(0, 8);

        //  Put program content number
        content.putByteOne(contentData.size());

        //  Put default  1 byte zero
        content.putByteOne(0);

        // Put each content's data
        for (JContentBase item : contentData) {
            content.putBytes(item.get());
        }

        return content.toByteArray();
    }


}