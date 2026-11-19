/* EasyKnack's portable, encoder-only LAME 3.100 configuration. Upstream sources
 * remain unmodified; no architecture-specific assembler or decoder is linked. */
#pragma once
#include <stdint.h>
#define STDC_HEADERS 1
#define HAVE_STDINT_H 1
#define HAVE_INTTYPES_H 1
#define HAVE_ERRNO_H 1
#define HAVE_FCNTL_H 1
#define HAVE_LIMITS_H 1
#define HAVE_MEMCPY 1
#define HAVE_STRCHR 1
#define HAVE_STRERROR 1
#define HAVE_STRTOL 1
#define HAVE_LONG_DOUBLE 1
#define HAVE_STRINGS_H 1
#define HAVE_SYS_TYPES_H 1
#define HAVE_SYS_STAT_H 1
#define HAVE_UNISTD_H 1
#define HAVE_LOG10 1
#define HAVE_LOG 1
#define HAVE_POW 1
#define PROTOTYPES 1
#define LAME_LIBRARY_BUILD 1
#define USE_FAST_LOG 1
#define PACKAGE "lame"
#define PACKAGE_NAME "lame"
#define PACKAGE_VERSION "3.100"
#define VERSION "3.100"
#define SIZEOF_SHORT 2
#define SIZEOF_INT 4
#define SIZEOF_LONG __SIZEOF_LONG__
#define SIZEOF_FLOAT 4
#define SIZEOF_DOUBLE 8
#define SIZEOF_LONG_DOUBLE __SIZEOF_LONG_DOUBLE__
typedef float ieee754_float32_t;
typedef double ieee754_float64_t;
typedef long double ieee854_float80_t;
