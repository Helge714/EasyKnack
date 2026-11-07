#define _USE_MATH_DEFINES
#include <cmath>
#ifndef M_PI
#define M_PI 3.141592653589793238462643383279502884
#endif
// Loris is GPL-2.0-or-later; see third_party/loris/COPYING and PROVENANCE.md.
// Unity compilation keeps host/plugin/test builds on the same source set.
#define Pi Loris_Analyzer_Pi
#include "../../third_party/loris/Analyzer.cpp"
#undef Pi
#define Pi Loris_AssociateBandwidth_Pi
#include "../../third_party/loris/AssociateBandwidth.cpp"
#undef Pi
#define Pi Loris_Breakpoint_Pi
#include "../../third_party/loris/Breakpoint.cpp"
#undef Pi
#define Pi Loris_BreakpointUtils_Pi
#include "../../third_party/loris/BreakpointUtils.cpp"
#undef Pi
#define Pi Loris_Envelope_Pi
#include "../../third_party/loris/Envelope.cpp"
#undef Pi
#define Pi Loris_F0Estimate_Pi
#include "../../third_party/loris/F0Estimate.cpp"
#undef Pi
#define Pi Loris_FourierTransform_Pi
#include "../../third_party/loris/FourierTransform.cpp"
#undef Pi
#define Pi Loris_FrequencyReference_Pi
#include "../../third_party/loris/FrequencyReference.cpp"
#undef Pi
#define Pi Loris_Fundamental_Pi
#include "../../third_party/loris/Fundamental.cpp"
#undef Pi
#define Pi Loris_KaiserWindow_Pi
#include "../../third_party/loris/KaiserWindow.cpp"
#undef Pi
#define Pi Loris_LinearEnvelope_Pi
#include "../../third_party/loris/LinearEnvelope.cpp"
#undef Pi
#define Pi Loris_LorisExceptions_Pi
#include "../../third_party/loris/LorisExceptions.cpp"
#undef Pi
#define Pi Loris_Notifier_Pi
#include "../../third_party/loris/Notifier.cpp"
#undef Pi
#define wrapPi Loris_Partial_wrapPi
#define Pi Loris_Partial_Pi
#include "../../third_party/loris/Partial.cpp"
#undef Pi
#undef wrapPi
#define Pi Loris_PartialBuilder_Pi
#include "../../third_party/loris/PartialBuilder.cpp"
#undef Pi
#define Pi Loris_PartialList_Pi
#include "../../third_party/loris/PartialList.cpp"
#undef Pi
#define Pi Loris_PartialUtils_Pi
#include "../../third_party/loris/PartialUtils.cpp"
#undef Pi
#define Pi Loris_phasefix_Pi
#include "../../third_party/loris/phasefix.cpp"
#undef Pi
#define Pi Loris_ReassignedSpectrum_Pi
#include "../../third_party/loris/ReassignedSpectrum.cpp"
#undef Pi
#define Pi Loris_SpectralPeakSelector_Pi
#include "../../third_party/loris/SpectralPeakSelector.cpp"
#undef Pi
#define Pi Loris_Filter_Pi
#include "../../third_party/loris/Filter.cpp"
#undef Pi
#include "../../third_party/loris/fftsg.c"
#include "../LorisStreamImplementation.hpp"
