# GpJSON - Leveraging Structural Indexes for High-Performance JSON Data Processing on GPUs
This Truffle language exposes JSONPath query execution to the polyglot [GraalVM](http://www.graalvm.org).

The goals are:
 1. Present a couple of parsing techniques based on structural indexes to quickly execute queries on JSON files
 2. Introduce a batching approach to improve performance and allow the processing of datasets bigger than the GPU’s memory
 3. Implement the above concepts into a Truffle Language to provide an engine that can be used from any host language that can run on the GraalVM.
 
## Using GpJSON in the GraalVM
To compile a JAR file containing GpJSON move to the language folder and run ```mvn package```.

Next, copy the JAR file from ```target/gpjson.jar``` into `jre/languages/gpjson` (Java 8) or `languages/gpjson` (Java 11) of the Graal installation. 

Note that `--jvm` and `--polyglot` must be specified in both cases as well.

In the examples folder, you can find a couple of files containing examples of the GpJSON's syntax.

## Benchmarks suite

To run the benchmarks provided in the [`benchmarks`](./benchmarks/) folder you first need to install the following dependencies:

- [CUDA Toolkit](https://developer.nvidia.com/cuda-downloads)
- [GraalVM Community Edition](https://github.com/graalvm/graalvm-ce-builds/releases)
- [gcc](https://gcc.gnu.org)
- [Node.js](https://nodejs.org/en)

Then, add the following variables to your `.bashrc` (or equivalent):

```
export CUDA_DIR=[your-cuda-path]
export PATH=$PATH:$CUDA_DIR/bin
export GRAAL_DIR=[your-graalvm-path]
export PATH=$PATH:$GRAAL_DIR/bin
export NODE_DIR=[your-node-path]
```

Copy the `grcuda` and `gpjson` JARs from the [`deliverables`](./deliverables/) folder to `[your-graalvm-path]/languages/[grcuda/gpjson]/`.

Move to the benchmarks folder `cd benchmarks` and run `make setup` to install jsonpath, jsonpath-plus and simdjson.

Finally, run `./[name-of-the-benchmark].sh`. Results will be saved to `[name-of-the-benchmark].csv`.

The following options can be added to the command above:

- `-g` to exclude the GPU-based benchmarks (GpJSON only). Default is `false`
- `-w [number]` to set the number of warmup runs. Default is `5`
- `-r [number]` to set the number of runs. Default is `10`
- `-t [number]` to set the number of threads (Java JSONPath only). Default is `11`
- `-d [path]` to set the path of the dataset. Default value is `/home/ubuntu/datasets-ext/`

Datasets can be downloaded [here](https://polimi365-my.sharepoint.com/:f:/g/personal/10604455_polimi_it/ElAPYQNeE1BLtcyR_BbFGS0BcaFPp2NiF1kGM1MtjFjmLA).

For further details, such as the versions of the dependencies used or the queries executed by the benchmarks suite, please refer to the official thesis and/or publication.
