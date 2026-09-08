/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.locks;
import java.util.concurrent.TimeUnit;

public interface Lock {
    //阻塞式获取，在没有获取到锁时，当前线程将会阻塞，不会参与线程调度，直到获取到锁为止，获取锁的过程中不响应中断。
    void lock();
    //阻塞式获取，并且可中断，该方法将在以下两种情况之一发生的情况下抛出InterruptedException。在InterruptedException抛出后，当前线程的中断标志位将会被清除。
    //1.在调用该方法时，线程的中断标志位已经被设为true了
    //2.在获取锁的过程中，线程被中断了，并且锁的获取实现会响应这个中断
    void lockInterruptibly() throws InterruptedException;
    //非阻塞式获取，从名字中也可以看出，try就是试一试的意思。无论成功与否，该方法都是立即返回的。
    // 相比前面两种阻塞式获取的方式该方法是有返回值的，获取锁成功了则返回tue，获取锁失败了则返回false。
    boolean tryLock();
    //带超时机制，并且可中断
    //1.如果可以获取带锁，则立即返回tue
    //2.如果获取不到锁，则当前线程将会休眠，不会参与线程调度，直到以下三个条件之一被满足：
    //2.1当前线程获取到了锁
    //2.2其它线程中断了当前线程
    //2.3设定的超时时间到了
    //3.该方法将在以下两种情况之一发生的情况下抛出InterruptedException
    //3.1在调用该方法时，线程的中断标志位已经被设为tue了
    //3.2在获取锁的过程中，线程被中断了，并且锁的获取实现会响应这个中断
    //4 在InterruptedException抛出后，当前线程的中断标志位将会被清除。
    //5 如果超时时间到了，当前线程还没有获得锁，则会直接返回false(注意,这里并没有抛出超时异常)。
    // 其实,tryLock(long time, TimeUnit unit)更像是阻塞式与非阻塞式的结合体，即在一定条件下(超时时间内,没有中断发生)阻塞，不满足这个条件则立即返回 (非阻塞)。
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
    // 值得注意的是，只有拥有的锁的线程才能释放锁，并且，必须显式地释放锁，这一点和离开同步代码块就自动被释放的监视器锁是不同的。
    void unlock();
    Condition newCondition();
}

