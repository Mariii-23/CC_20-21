package module.commom;

import java.util.Objects;

public final class Pair<P, E> {
  private final P first;
  private final E second;

  public Pair(P first, E second) {
    this.first = first;
    this.second = second;
  }

  public P getFirst() {
    return first;
  }

  public E getSecond() {
    return second;
  }

  public P first() {
    return first;
  }

  public E second() {
    return second;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (Pair) obj;
    return Objects.equals(this.first, that.first) &&
        Objects.equals(this.second, that.second);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }

  @Override
  public String toString() {
    return "Pair[" +
        "first=" + first + ", " +
        "second=" + second + ']';
  }

}
