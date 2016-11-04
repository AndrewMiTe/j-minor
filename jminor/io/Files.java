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
 *
 * @author Andrew M. Teller(https://github.com/AndrewMiTe)
 */
public class Files {
  
  public static final Stream<Object> objects(Path path) throws IOException {
    ObjectFileIterator fileObjects = new ObjectFileIterator(path);
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
        fileObjects, Spliterator.IMMUTABLE), false)
        .onClose(() -> fileObjects.close())
    ;
  }
  
  private static class ObjectFileIterator implements Iterator<Object> {

    private ObjectInputStream input = null;
    
    private Path path;
    
    private Object nextObject = null;

    public ObjectFileIterator(Path path) {
      this.path = path;
    }
    
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
        }
      }
    }
    
    public void close() {
      try {
        input.close();
      }
      catch (IOException e) {        
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
        try {
          input.close();
        }
        catch (IOException f) {
        }
      }
      return returnValue;
    }
    
  }
  
}
