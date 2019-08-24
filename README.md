## ChatFilter v3

This is the official chat filter of [Craftathon](https://craftathon.org/), copied from the [Craftathon](https://github.com/Craftathon/) GitHub organization, originally made in mid-late December of 2018. This could be improved and may be later on. This is the third rewrite of the filter, and a full feature specification document may be found [here](https://github.com/RubbaBoy/ChatFilter/Specifications.md).

## Benchmarks

Just before releasing this repo I made some (very) small optimizations, and also added a benchmarking test to it. It is tested with SwearIpsum ([Example](https://rubbaboy.me/code/s82d84j)) generated in the SwearIpsum class. The current results are below, the long and short message lines are below respectively:

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

