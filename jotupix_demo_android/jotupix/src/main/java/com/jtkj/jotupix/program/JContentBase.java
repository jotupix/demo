package com.jtkj.jotupix.program;

/**
 * Base class for all content types used in program data.
 * <p>
 * This abstract class defines the interface and common properties for various
 * content types (text, animation, border, etc.). Each derived class must
 * implement its own data packaging method according to the communication
 * protocol requirements.
 */
public abstract class JContentBase {

    /**
     * Content type enumeration.
     * <p>
     * Indicates the specific category of graphical content being transferred.
     * Each type corresponds to different encoding or rendering rules.
     */
    public static class ContentType {
        public static int TEXT = 1;
        public static int GRAFFITI = 2;
        public static int ANIMATION = 3;
        public static int BORDER = 4;
        public static int FULLCOLOR = 5;
        public static int DIYCOLOR = 6;
        public static int GIF = 0x0C;
    }

    /**
     * Blending mode used when compositing content.
     * <p>
     * MIX   - Content blends with existing pixels.
     * COVER - Content overwrites existing pixels.
     */
    public static class BlendType {
        public static int MIX = 0;
        public static int COVER = 1;
    }

    public int contentType;

    /**
     * Pack content data according to the protocol format.
     * <p>
     * Each derived class must implement this function to generate the
     * protocol-compliant byte sequence that represents its content.
     *
     * @return A byte buffer containing the packaged content.
     */
    public abstract byte[] get();

}
