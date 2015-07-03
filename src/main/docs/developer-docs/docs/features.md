# Features and Extractors
Lire supports global features as well as local features. There are proper interfaces for each type of features. Following, you can find a brief description of the available interfaces, that you can follow to implement and integrate your own features.

## LireFeature
`LireFeature` is the basic interface for all content based features and extends the `FeatureVector` interface. The `FeatureVector` interface contains the `getFeatureVector()` method, which you can use in order to get the feature's vector as a double[] array. `LireFeature` is extended by two interfaces, the `GlobalFeature` and the `LocalFeature`.

### Global Features
Global features, like `CEDD` or `AutoColorCorrelogram`, should implement the `GlobalFeature` interface. That interface extends both `LireFeature` and `Extractor` interfaces. This means that a global features holds both the feature and the `extract()` method.

### Local Features
On the other hand, due to the fact that local feature extractors create a lot of local features, there are two interfaces that should be used in that case. The `LocalFeatureExtractor`, which extends the `Extractor` interface and contains the `extract()` method, and the `LocalFeature`, which extends the `LireFeature` interface.