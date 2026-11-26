#pragma once
#ifdef _WIN32
#include "../WindowsMp3Recording.hpp"
namespace easyknack::platform {using Mp3Recording=WindowsMp3Recording;}
#else
#include "LameMp3Recording.hpp"
namespace easyknack::platform {using Mp3Recording=LameMp3Recording;}
#endif
