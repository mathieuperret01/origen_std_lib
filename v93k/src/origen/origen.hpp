#ifndef ORIGEN_HPP_INCLUDED
#define ORIGEN_HPP_INCLUDED

#define ORIGEN_VERSION "0.0.1"

#ifndef debugger
#define debugger __asm__("int $3");
#endif

#include "origen/utils.hpp"

namespace Origen {

Utils::Version version();

}

#endif