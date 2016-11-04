/*
 * MIT License
 *
 * Copyright (c) 2016 Andrew Michael Teller(https://github.com/AndrewMiTe)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jminor.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An expansion of the functionality found in the {@link java.nio.file.Files}
 * class, which is part of Java's NIO 2 API. Primarily this is the addition of
 * an {@link #objects(java.nio.file.Path) objects} method that returns a {@link
 * java.util.stream.Stream} of objects from the file located in the given path.
 * @author Andrew M. Teller(https://github.com/AndrewMiTe)
 */
public class Files {
  
  /**
   * Returns a stream of objects read from the given path. The file that the 
   * path points to is not opened when the stream is made, but rather the
   * resource is only accessed when a terminal method is called on the stream. 
   * @param path path to the file to read object from.
   * @return stream of objects read from a file.
   */
  public static final Stream<Object> objects(Path path) {
    ObjectFileIterator fileObjects = new ObjectFileIterator(path);
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
        fileObjects, Spliterator.IMMUTABLE), false)
        .onClose(fileObjects::close)
    ;
  }
  
  /**
   * Delivers the next object to be read from a file as the calling object
   * iterates over it. Any excretions to result from erroneous access to the
   * given path will be silently ignored by this object, generally resulting in
   * the next object being {@code null}.
   */
  private static class ObjectFileIterator implements Iterator<Object> {

    /**
     * Input stream from which the objects are read from.
     */
    private ObjectInputStream input = null;
    
    /**
     * Path to the file to be read.
     */
    private final Path path;
    
    /**
     * Object read from the file that is next in the iteration.
     */
    private Object nextObject = null;

    /**
     * Initializes the object with the path of the assumed file to be read.
     * @param path 
     */
    public ObjectFileIterator(Path path) {
      this.path = path;
    }
    
    /**
     * Opens the file to be read. Silently ignores any exceptions from erroneous
     * access to the file.
     */
    private void openInput() {
      try {
        FileInputStream fileIn = new FileInputStream(path.toFile());
        BufferedInputStream bufferedIn = new BufferedInputStream(fileIn);
        input = new ObjectInputStream(bufferedIn);
        nextObject = input.readObject();
      }
      catch (IOException | ClassNotFoundException e) {
        nextObject = null;
        try {
          input.close();
        }
        catch (IOException f) {
        // Silient catch.
        }
      }
    }
    
    /**
     * Opens the file to be read. Silently ignores any exceptions from erroneous
     * access to the file.
     */
    public void close() {
      try {
        input.close();
      }
      catch (IOException e) {     
        // Silient catch.
      }
    }
    
    @Override // from Iterator
    public boolean hasNext() {
      if (input == null) openInput();
      return nextObject != null;
    }

    @Override // from Iterator
    public Object next() {
      Object returnValue = nextObject;
      nextObject = null;
      try {
        nextObject = input.readObject();
      }
      catch (IOException | ClassNotFoundException e) {
        close();
      }
      return returnValue;
    }
    
  }
  
}
