## ChatFilter v3

This is the official chat filter of [Craftathon](https://craftathon.org/), copied from the [Craftathon](https://github.com/Craftathon/) GitHub organization, originally made in mid-late December of 2018. This could be improved and may be later on. This is the third rewrite of the filter, and a full feature specification document may be found [here](https://github.com/RubbaBoy/ChatFilter/blob/master/Specifications.md) ([or on Google Docs](https://docs.google.com/document/d/1AangW6Jv_X2TmRq8W-VM0TtWohxPGtAXCM7xruTTTjU/edit?usp=sharing)), which is highly recommended if you are interested in this.

- [Benchmarks](#Benchmarks)
- [Filter Examples](#Filter-Examples)
- [Code Examples](#Code-Examples)

## Benchmarks

Just before releasing this repo I made some (very) small optimizations, and also added a benchmarking test to it. It is tested with SwearIpsum ([Example](https://rubbaboy.me/code/s82d84j)) generated in the [SwearIpsum](https://github.com/RubbaBoy/ChatFilter/blob/master/benchmark/src/main/java/com/craftathon/chatfilter3/utils/SwearIpsum.java#L39) class. The current results are below, the long and short message lines are below respectively:

```
biᾰὰáṱṯcch ante ipsum bbbo¤ḡ horny chhỗợoḋȅ pu➎şi ẉwaặﬡk hoⓡnie ultrices sἤἥἤὴiｔ ｍủuứṻfｆ Curae; Aenean neὲgｇrổ wᾧaaἅanｎk @ậ➍s５５ lube lacinia fửccck tincidunt vitae
```

```
coccc¢k cṻnt ultrices nibh
```

And the results (20 warmups and 40 iterations)

```
# Run complete. Total time: 00:04:05

Benchmark                              Mode  Cnt     Score    Error  Units
BenchmarkChatFilter.filterSingleLong   avgt   40  4724.346 ± 24.654  us/op
BenchmarkChatFilter.filterSingleShort  avgt   40   892.203 ± 16.233  us/op
```

## Filter Examples

The following are some examples of filtered text, before and with the `ChatFilter#blockFullWord(boolean)` option enabled/disabled. Some are labeled with more specific features being demonstrated.

```diff
- Test of a fucking sh1tty @ss filter
+ Test of a ******* ****** *** filter
+ Test of a ****ing *****y *** filter
```

### Word Priorities

The word `ass` has a priority of 0, meaning it can't start with the word, but may contain it.
```diff
- Your @s5 is grass and i'm a lawnmower
+ Your *** is grass and i'm a lawnmower
+ Your *** is grass and i'm a lawnmower
```

The word `fuck` has a priority of 1, meaning it can't be contained within a word at all.
```diff
- That really oofuckingdd sucks
+ That really *********** sucks
+ That really oo****ingdd sucks
```

### No Separation

Words may be separated by spaces by default, unless set in the word list. An example is `sex`, which is set by default to be able to be separated by a space in between the `e` and `s`. A future addition to this feature (As it is still a bit buggy) is ensuring the word is encased in other letters.

```diff
- Give sex to me
+ Give *** to me
+ Give *** to me
```

The word `sex` with a space separating the `s` and `e`:
```diff
- His extra pizza
+ His extra pizza
+ His extra pizza
```

### Number Filter

By default, words are considered unrecognizable if consisting of 75% or more numbers.

33% number:
```diff
- 4ss
+ ***
+ ***
```

100% number:

```diff
- 455
+ 455
+ 455
```



## Code Examples

Using the filter is pretty simple. Soon the repo will probably be on Central, though right now it's just on JitPack. To obtain the rep, simply put

```groovy
repositories {
	maven { url 'https://jitpack.io' }
}
```

And

```groovy
dependencies {
	implementation 'com.github.RubbaBoy:ChatFilter:master-SNAPSHOT'
}
```

To your build.gradle. To use it with maven, just look [here](https://jitpack.io/#RubbaBoy/ChatFilter/master-SNAPSHOT) because I can't be fucked to fill half this readme with the verbose shit maven is.

To use the filter, you need to get an instance of `ChatFilter` via

```java
var chatFilter = new DefaultChatFilter();
chatFilter.init();
```

Then to filter words, just do

```java
var cleaned = chatFilter.clean("A fuucking string shiiiittt right here.");
```

And you can use whatever string you want. This string results in `A ******** string ********* right here.`.

To add custom bad words, you can get the default HashMap of them and just add to that, or give it a custom map. The method below gets the default bad words, and adds the given map to a copy of them.

```java
chatFilter.init(DefaultChatFilter.getBlocked(Map.of("dog", 1)));
```

This acts as expected, with the following code and output after being initialized with that

```java
var cleaned = chatFilter.clean("I like dogs, really any king of d0g.");
```

```
I like ***** really any king of ****
```

