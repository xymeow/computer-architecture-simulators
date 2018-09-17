# Cache coherence simulators
There are two simulators for cache coherence I wrote when taking the course Computer Architecture.

- Bus-snooping protocol: snoopingindex.html & files in js/
- Directory-based protocol: dirindex.html & files in js-dir/

## Usage
Just open the index in your browser.

The 'system' consists of 4 cpus and memory, you can modify the Address and Data for read(R) and write(W) operations. Also, you can see the statistics of your operations.

I think rewriting these in React would be better, you can have a try :)

# cache一致性模拟器

本文件夹中包括snooping监听法模拟器和directory目录法模拟器

snoopingindex.html为监听法，点开即运行，建议使用chrome浏览器（用旧版可能有不兼容）
dirindex.html为目录法，点开即运行

js中包括监听法的代码实现，js-dir包括目录法的代码实现