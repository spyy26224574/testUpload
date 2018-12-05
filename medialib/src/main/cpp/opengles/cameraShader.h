#ifndef _CM_CAMAERA_SHADER_
#define _CM_CAMAERA_SHADER_

#define DEBUG 0

#include "./esUtil.h"
#include "./matrix.h"

void
drawFrame(void* ins);

void
bindTexture(GLenum texture_n, unsigned int texture_id, GLsizei width, GLsizei height, const void * buffer);



#endif
