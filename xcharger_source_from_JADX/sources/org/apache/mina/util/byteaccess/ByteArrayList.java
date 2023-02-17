package org.apache.mina.util.byteaccess;

import java.util.NoSuchElementException;

class ByteArrayList {
    private int firstByte;
    /* access modifiers changed from: private */
    public final Node header = new Node();
    private int lastByte;

    protected ByteArrayList() {
    }

    public int lastByte() {
        return this.lastByte;
    }

    public int firstByte() {
        return this.firstByte;
    }

    public boolean isEmpty() {
        return this.header.next == this.header;
    }

    public Node getFirst() {
        return this.header.getNextNode();
    }

    public Node getLast() {
        return this.header.getPreviousNode();
    }

    public void addFirst(ByteArray ba) {
        addNode(new Node(ba), this.header.next);
        this.firstByte -= ba.last();
    }

    public void addLast(ByteArray ba) {
        addNode(new Node(ba), this.header);
        this.lastByte += ba.last();
    }

    public Node removeFirst() {
        Node node = this.header.getNextNode();
        this.firstByte += node.f199ba.last();
        return removeNode(node);
    }

    public Node removeLast() {
        Node node = this.header.getPreviousNode();
        this.lastByte -= node.f199ba.last();
        return removeNode(node);
    }

    /* access modifiers changed from: protected */
    public void addNode(Node nodeToInsert, Node insertBeforeNode) {
        Node unused = nodeToInsert.next = insertBeforeNode;
        Node unused2 = nodeToInsert.previous = insertBeforeNode.previous;
        Node unused3 = insertBeforeNode.previous.next = nodeToInsert;
        Node unused4 = insertBeforeNode.previous = nodeToInsert;
    }

    /* access modifiers changed from: protected */
    public Node removeNode(Node node) {
        Node unused = node.previous.next = node.next;
        Node unused2 = node.next.previous = node.previous;
        boolean unused3 = node.removed = true;
        return node;
    }

    public class Node {
        /* access modifiers changed from: private */

        /* renamed from: ba */
        public ByteArray f199ba;
        /* access modifiers changed from: private */
        public Node next;
        /* access modifiers changed from: private */
        public Node previous;
        /* access modifiers changed from: private */
        public boolean removed;

        private Node() {
            this.previous = this;
            this.next = this;
        }

        private Node(ByteArray ba) {
            if (ba == null) {
                throw new IllegalArgumentException("ByteArray must not be null.");
            }
            this.f199ba = ba;
        }

        public Node getPreviousNode() {
            if (hasPreviousNode()) {
                return this.previous;
            }
            throw new NoSuchElementException();
        }

        public Node getNextNode() {
            if (hasNextNode()) {
                return this.next;
            }
            throw new NoSuchElementException();
        }

        public boolean hasPreviousNode() {
            return this.previous != ByteArrayList.this.header;
        }

        public boolean hasNextNode() {
            return this.next != ByteArrayList.this.header;
        }

        public ByteArray getByteArray() {
            return this.f199ba;
        }

        public boolean isRemoved() {
            return this.removed;
        }
    }
}
