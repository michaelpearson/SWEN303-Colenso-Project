package database.model;

import java.util.Stack;

public class TaggerThread extends Thread {
    class Tag {
        TeiDocument documentToTag;
        String tagType;
        String tagValue;

        public Tag(TeiDocument documentToTag, String tagType, String tagValue) {
            this.documentToTag = documentToTag;
            this.tagType = tagType;
            this.tagValue = tagValue;
        }
    }

    Stack<Tag> documentsToTag = new Stack<>();

    @Override
    public void run() {
        super.run();
    }
}
