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
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Contains static methods for the manipulation of files using an object {@link
 * java.util.stream.Stream}, as well as methods for additional manipulation of
 * these streams.
 * @author Andrew M. Teller(https://github.com/AndrewMiTe)
 */
public class ObjectFiles {
  
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
        .onClose(fileObjects::close);
  }
  
  public static final ObjectStream toObjectStream(Stream<Object> stream) {
    return new ObjectStreamWrapper(stream);
  }

  /**
   * Delivers the next object to be read from a file as the calling object
   * iterates over it. Any exceptions to result from erroneous access to the
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
        close();
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

  /**
   * Wraps a given Stream object with to give it additional methods for writing
   * the objects of the stream to a file or to perform intermediate operations
   * that assume the objects are read from a file.
   */
  private static class ObjectStreamWrapper implements ObjectStream {

    /**
     * The wrapped Stream object;
     */    
    private Stream<Object> stream;
    
    /**
     * Initializes with the given Stream object to be wrapped.
     * @param stream 
     */
    public ObjectStreamWrapper(Stream<Object> stream) {
      this.stream = stream;
    }

    @Override // from ObjectStream
    public void write(Path path) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override // from Stream
    public Stream<Object> filter(Predicate<? super Object> predicate) {
      stream = stream.filter(predicate);
      return this;
    }

    @Override // from Stream
    public <R> Stream<R> map(Function<? super Object, ? extends R> mapper) {
      return stream.map(mapper);
    }

    @Override // from Stream
    public IntStream mapToInt(ToIntFunction<? super Object> mapper) {
      return stream.mapToInt(mapper);
    }

    @Override // from Stream
    public LongStream mapToLong(ToLongFunction<? super Object> mapper) {
      return stream.mapToLong(mapper);
    }

    @Override // from Stream
    public DoubleStream mapToDouble(ToDoubleFunction<? super Object> mapper) {
      return stream.mapToDouble(mapper);
    }

    @Override // from Stream
    public <R> Stream<R> flatMap(Function<? super Object, ? extends Stream<? extends R>> mapper) {
      return stream.flatMap(mapper);
    }

    @Override // from Stream
    public IntStream flatMapToInt(Function<? super Object, ? extends IntStream> mapper) {
      return stream.flatMapToInt(mapper);
    }

    @Override // from Stream
    public LongStream flatMapToLong(Function<? super Object, ? extends LongStream> mapper) {
      return stream.flatMapToLong(mapper);
    }

    @Override // from Stream
    public DoubleStream flatMapToDouble(Function<? super Object, ? extends DoubleStream> mapper) {
      return stream.flatMapToDouble(mapper);
    }

    @Override // from Stream
    public Stream<Object> distinct() {
      stream = stream.distinct();
      return this;
    }

    @Override // from Stream
    public Stream<Object> sorted() {
      stream = stream.sorted();
      return this;
    }

    @Override // from Stream
    public Stream<Object> sorted(Comparator<? super Object> comparator) {
      stream = stream.sorted(comparator);
      return this;
    }

    @Override // from Stream
    public Stream<Object> peek(Consumer<? super Object> action) {
      stream = stream.peek(action);
      return this;
    }

    @Override // from Stream
    public Stream<Object> limit(long maxSize) {
      stream = stream.limit(maxSize);
      return this;
    }

    @Override // from Stream
    public Stream<Object> skip(long n) {
      stream = stream.skip(n);
      return this;
    }

    @Override // from Stream
    public void forEach(Consumer<? super Object> action) {
      stream.forEach(action);
    }

    @Override // from Stream
    public void forEachOrdered(Consumer<? super Object> action) {
      stream.forEachOrdered(action);
    }

    @Override // from Stream
    public Object[] toArray() {
      return stream.toArray();
    }

    @Override // from Stream
    public <A> A[] toArray(IntFunction<A[]> generator) {
      return stream.toArray(generator);
    }

    @Override // from Stream
    public Object reduce(Object identity, BinaryOperator<Object> accumulator) {
      return stream.reduce(identity, accumulator);
    }

    @Override // from Stream
    public Optional<Object> reduce(BinaryOperator<Object> accumulator) {
      return stream.reduce(accumulator);
    }

    @Override // from Stream
    public <U> U reduce(U identity, BiFunction<U, ? super Object, U> accumulator, BinaryOperator<U> combiner) {
      return stream.reduce(identity, accumulator, combiner);
    }

    @Override // from Stream
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super Object> accumulator, BiConsumer<R, R> combiner) {
      return stream.collect(supplier, accumulator, combiner);
    }

    @Override // from Stream
    public <R, A> R collect(Collector<? super Object, A, R> collector) {
      return stream.collect(collector);
    }

    @Override // from Stream
    public Optional<Object> min(Comparator<? super Object> comparator) {
      return stream.min(comparator);
    }

    @Override // from Stream
    public Optional<Object> max(Comparator<? super Object> comparator) {
      return stream.max(comparator);
    }

    @Override // from Stream
    public long count() {
      return stream.count();
    }

    @Override // from Stream
    public boolean anyMatch(Predicate<? super Object> predicate) {
      return stream.anyMatch(predicate);
    }

    @Override // from Stream
    public boolean allMatch(Predicate<? super Object> predicate) {
      return stream.allMatch(predicate);
    }

    @Override // from Stream
    public boolean noneMatch(Predicate<? super Object> predicate) {
      return stream.noneMatch(predicate);
    }

    @Override // from Stream
    public Optional<Object> findFirst() {
      return stream.findFirst();
    }

    @Override // from Stream
    public Optional<Object> findAny() {
      return stream.findAny();
    }

    @Override // from Stream
    public Iterator<Object> iterator() {
      return stream.iterator();
    }

    @Override // from Stream
    public Spliterator<Object> spliterator() {
      return stream.spliterator();
    }

    @Override // from Stream
    public boolean isParallel() {
      return stream.isParallel();
    }

    @Override // from Stream
    public Stream<Object> sequential() {
      stream = stream.sequential();
      return this;
    }

    @Override // from Stream
    public Stream<Object> parallel() {
      stream = stream.parallel();
      return this;
    }

    @Override // from Stream
    public Stream<Object> unordered() {
      stream = stream.unordered();
      return this;
    }

    @Override // from Stream
    public Stream<Object> onClose(Runnable closeHandler) {
      stream = stream.onClose(closeHandler);
      return this;
    }

    @Override // from Stream
    public void close() {
      stream.close();
    }
  }
  
}
