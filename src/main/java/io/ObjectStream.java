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

package io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * A stream that allows for the objects to be written to a file as a terminal
 * operation.
 * @author Andrew M. Teller(https://github.com/AndrewMiTe)
 */
public interface ObjectStream extends Stream<Object> {
  
  /**
   * Writes the output of the stream to the file of the given path.
   * <p>This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
   * @param path path to write to.
   * @throws IOException if the file cannot be accessed or if the file fails to
   *         be written to.
   */
  void write(Path path) throws IOException;
  
}
