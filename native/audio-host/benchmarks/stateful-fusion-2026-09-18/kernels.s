	.def	@feat.00;
	.scl	3;
	.type	0;
	.endef
	.globl	@feat.00
.set @feat.00, 0
	.intel_syntax noprefix
	.file	"StatefulFusionBenchmark.cpp"
	.def	block_chain;
	.scl	2;
	.type	32;
	.endef
	.text
	.globl	block_chain                     # -- Begin function block_chain
	.p2align	4
block_chain:                            # @block_chain
.seh_proc block_chain
# %bb.0:
	push	r14
	.seh_pushreg r14
	push	rsi
	.seh_pushreg rsi
	push	rdi
	.seh_pushreg rdi
	push	rbx
	.seh_pushreg rbx
	sub	rsp, 408
	.seh_stackalloc 408
	movaps	xmmword ptr [rsp + 384], xmm6   # 16-byte Spill
	.seh_savexmm xmm6, 384
	.seh_endprologue
	mov	rsi, rcx
	lea	rbx, [rcx + 312]
	movsxd	rdi, edx
	movss	xmm0, dword ptr [rcx + 64]      # xmm0 = mem[0],zero,zero,zero
	cvtss2sd	xmm3, xmm0
	xorps	xmm6, xmm6
	movups	xmmword ptr [rsp + 40], xmm6
	movabs	r14, 4676829883349860352
	mov	qword ptr [rsp + 32], r14
	mov	byte ptr [rsp + 56], 1
	mov	rdx, rbx
	mov	r8, rdi
	call	_ZN9easyknack14OscillatorNode6renderEPfyddPKfS3_b
	mov	qword ptr [rsp + 64], rbx
	mov	qword ptr [rsp + 72], 0
	lea	rax, [rsp + 64]
	mov	qword ptr [rsp + 248], rax
	mov	qword ptr [rsp + 256], 2
	lea	rbx, [rsi + 1336]
	mov	qword ptr [rsp + 264], rbx
	mov	qword ptr [rsp + 272], rdi
	mov	qword ptr [rsp + 280], r14
	movups	xmmword ptr [rsp + 288], xmm6
	movups	xmmword ptr [rsp + 304], xmm6
	mov	qword ptr [rsp + 320], 0
	mov	qword ptr [rsp + 328], 1
	lea	rcx, [rsi + 96]
	movups	xmmword ptr [rsp + 336], xmm6
	movups	xmmword ptr [rsp + 352], xmm6
	movups	xmmword ptr [rsp + 368], xmm6
	lea	rdx, [rsp + 248]
	call	_ZN9easyknack8MathNode7processERNS_14ProcessContextE
	mov	qword ptr [rsp + 80], rbx
	movups	xmmword ptr [rsp + 88], xmm6
	lea	rax, [rsp + 80]
	mov	qword ptr [rsp + 112], rax
	mov	qword ptr [rsp + 120], 3
	lea	rax, [rsi + 2360]
	mov	qword ptr [rsp + 128], rax
	mov	qword ptr [rsp + 136], rdi
	mov	qword ptr [rsp + 144], r14
	movups	xmmword ptr [rsp + 152], xmm6
	movups	xmmword ptr [rsp + 168], xmm6
	mov	qword ptr [rsp + 184], 0
	mov	qword ptr [rsp + 192], 1
	add	rsi, 176
	movups	xmmword ptr [rsp + 200], xmm6
	movups	xmmword ptr [rsp + 216], xmm6
	movups	xmmword ptr [rsp + 232], xmm6
	lea	rdx, [rsp + 112]
	mov	rcx, rsi
	call	_ZN9easyknack10FilterNode7processERNS_14ProcessContextE
	movaps	xmm6, xmmword ptr [rsp + 384]   # 16-byte Reload
	add	rsp, 408
	pop	rbx
	pop	rdi
	pop	rsi
	pop	r14
	ret
	.seh_endproc
                                        # -- End function
	.def	inline_blocks;
	.scl	2;
	.type	32;
	.endef
	.globl	inline_blocks                   # -- Begin function inline_blocks
	.p2align	4
inline_blocks:                          # @inline_blocks
# %bb.0:
	jmp	_Z12block_kernelILb1EEvR5Chaini # TAILCALL
                                        # -- End function
	.def	_Z12block_kernelILb1EEvR5Chaini;
	.scl	2;
	.type	32;
	.endef
	.section	.rdata,"dr"
	.p2align	4, 0x0                          # -- Begin function _Z12block_kernelILb1EEvR5Chaini
.LCPI2_0:
	.quad	0x3f7107fa21792a00              # double 0.0041579981548900413
.LCPI2_1:
	.quad	0x40d3880000000000              # double 2.0E+4
.LCPI2_2:
	.quad	0x40e7700000000000              # double 48000
.LCPI2_3:
	.quad	0x3fe0000000000000              # double 0.5
.LCPI2_4:
	.quad	0x4010000000000000              # double 4
.LCPI2_5:
	.quad	0x4020000000000000              # double 8
.LCPI2_6:
	.quad	0x4030000000000000              # double 16
.LCPI2_7:
	.quad	0x4040000000000000              # double 32
.LCPI2_8:
	.quad	0x4050000000000000              # double 64
.LCPI2_9:
	.quad	0x4060000000000000              # double 128
.LCPI2_10:
	.quad	0x4070000000000000              # double 256
.LCPI2_11:
	.quad	0x4080000000000000              # double 512
.LCPI2_12:
	.quad	0x401921fb54442d18              # double 6.2831853071795862
.LCPI2_13:
	.quad	0x40a0000000000000              # double 2048
.LCPI2_14:
	.quad	0x43e0000000000000              # double 9.2233720368547758E+18
	.zero	8
.LCPI2_15:
	.quad	0x7fffffffffffffff              # double NaN
	.quad	0x7fffffffffffffff              # double NaN
.LCPI2_16:
	.quad	0x7ff0000000000000              # double +Inf
	.quad	0x7ff0000000000000              # double +Inf
.LCPI2_17:
	.quad	0xbff0000000000000              # double -1
	.quad	0xbff0000000000000              # double -1
.LCPI2_18:
	.quad	0x3ff0000000000000              # double 1
	.quad	0x3ff0000000000000              # double 1
.LCPI2_19:
	.long	1127219200                      # 0x43300000
	.long	1160773632                      # 0x45300000
	.long	0                               # 0x0
	.long	0                               # 0x0
.LCPI2_20:
	.quad	0x4330000000000000              # double 4503599627370496
	.quad	0x4530000000000000              # double 1.9342813113834067E+25
.LCPI2_21:
	.long	0x3b883fd1                      # float 0.00415799813
	.long	0x3b883fd1                      # float 0.00415799813
	.zero	4
	.zero	4
.LCPI2_22:
	.long	0x80000000                      # float -0
	.long	0x80000000                      # float -0
	.long	0x80000000                      # float -0
	.long	0x80000000                      # float -0
.LCPI2_23:
	.long	0x7fffffff                      # float NaN
	.long	0x7fffffff                      # float NaN
	.long	0x7fffffff                      # float NaN
	.long	0x7fffffff                      # float NaN
.LCPI2_24:
	.long	0x2b8cbccc                      # float 9.99999996E-13
.LCPI2_25:
	.long	0x3f800000                      # float 1
.LCPI2_26:
	.long	0xbf800000                      # float -1
	.zero	4
.LCPI2_27:
	.long	2146435071                      # 0x7fefffff
	.long	2146435071                      # 0x7fefffff
	.long	2146435071                      # 0x7fefffff
	.long	2146435071                      # 0x7fefffff
.LCPI2_28:
	.quad	0x4014000000000000              # double 5
	.quad	0x3fb999999999999a              # double 0.10000000000000001
.LCPI2_29:
	.quad	0x40d5180000000000              # double 21600
	.quad	0x403e000000000000              # double 30
.LCPI2_30:
	.quad	0x408f400000000000              # double 1000
	.quad	0x3fe69fbe76c8b439              # double 0.70699999999999996
.LCPI2_31:
	.quad	0x3f7107fa21792a00              # double 0.0041579981548900413
	.quad	0x3f7107fa21792a00              # double 0.0041579981548900413
.LCPI2_32:
	.quad	0x40d5180000000000              # double 21600
.LCPI2_33:
	.quad	0x400921fb54442d18              # double 3.1415926535897931
.LCPI2_34:
	.quad	0x3ff69e9565708efd              # double 1.4137166941154071
.LCPI2_35:
	.quad	0x4014000000000000              # double 5
.LCPI2_36:
	.quad	0x3f357254c2c98977              # double 3.2724923474893676E-4
.LCPI2_37:
	.quad	0x403e000000000000              # double 30
.LCPI2_38:
	.quad	0x3fa1111111111111              # double 0.033333333333333333
.LCPI2_39:
	.quad	0x3ff0000000000000              # double 1
.LCPI2_40:
	.quad	0x4024000000000000              # double 10
.LCPI2_41:
	.quad	0x47efffffe0000000              # double 3.4028234663852886E+38
.LCPI2_42:
	.quad	0x7ff0000000000000              # double +Inf
	.zero	8
.LCPI2_43:
	.quad	0x3abef2d0f5da7dd9              # double 1.0E-25
	.quad	0x3abef2d0f5da7dd9              # double 1.0E-25
	.section	.text$_Z12block_kernelILb1EEvR5Chaini,"xr",discard,_Z12block_kernelILb1EEvR5Chaini
	.globl	_Z12block_kernelILb1EEvR5Chaini
	.p2align	4
_Z12block_kernelILb1EEvR5Chaini:        # @_Z12block_kernelILb1EEvR5Chaini
.seh_proc _Z12block_kernelILb1EEvR5Chaini
# %bb.0:
	push	r15
	.seh_pushreg r15
	push	r14
	.seh_pushreg r14
	push	r13
	.seh_pushreg r13
	push	r12
	.seh_pushreg r12
	push	rsi
	.seh_pushreg rsi
	push	rdi
	.seh_pushreg rdi
	push	rbp
	.seh_pushreg rbp
	push	rbx
	.seh_pushreg rbx
	sub	rsp, 216
	.seh_stackalloc 216
	movapd	xmmword ptr [rsp + 192], xmm15  # 16-byte Spill
	.seh_savexmm xmm15, 192
	movapd	xmmword ptr [rsp + 176], xmm14  # 16-byte Spill
	.seh_savexmm xmm14, 176
	movapd	xmmword ptr [rsp + 160], xmm13  # 16-byte Spill
	.seh_savexmm xmm13, 160
	movapd	xmmword ptr [rsp + 144], xmm12  # 16-byte Spill
	.seh_savexmm xmm12, 144
	movapd	xmmword ptr [rsp + 128], xmm11  # 16-byte Spill
	.seh_savexmm xmm11, 128
	movdqa	xmmword ptr [rsp + 112], xmm10  # 16-byte Spill
	.seh_savexmm xmm10, 112
	movapd	xmmword ptr [rsp + 96], xmm9    # 16-byte Spill
	.seh_savexmm xmm9, 96
	movapd	xmmword ptr [rsp + 80], xmm8    # 16-byte Spill
	.seh_savexmm xmm8, 80
	movapd	xmmword ptr [rsp + 64], xmm7    # 16-byte Spill
	.seh_savexmm xmm7, 64
	movapd	xmmword ptr [rsp + 48], xmm6    # 16-byte Spill
	.seh_savexmm xmm6, 48
	.seh_endprologue
	test	edx, edx
	je	.LBB2_46
# %bb.1:
	mov	rsi, rcx
	mov	dword ptr [rsp + 44], edx       # 4-byte Spill
	movsxd	rbx, edx
	movss	xmm0, dword ptr [rcx + 64]      # xmm0 = mem[0],zero,zero,zero
	xorps	xmm10, xmm10
	cvtss2sd	xmm10, xmm0
	mov	r15d, dword ptr [rcx + 80]
	imul	r12, r15, 81960
	add	r12, qword ptr [rcx + 88]
	movsd	xmm6, qword ptr [rcx + 56]      # xmm6 = mem[0],zero
	movsd	xmm11, qword ptr [rcx + 72]     # xmm11 = mem[0],zero
	xor	r13d, r13d
	movabs	r14, 9223372036854775807
	movabs	rbp, 9218868437227405312
	movsd	xmm8, qword ptr [rip + .LCPI2_1] # xmm8 = [2.0E+4,0.0E+0]
	movsd	xmm13, qword ptr [rip + .LCPI2_3] # xmm13 = [5.0E-1,0.0E+0]
	movsd	xmm15, qword ptr [rip + .LCPI2_12] # xmm15 = [6.2831853071795862E+0,0.0E+0]
	movapd	xmm14, xmmword ptr [rip + .LCPI2_16] # xmm14 = [+Inf,+Inf]
	movapd	xmm12, xmmword ptr [rip + .LCPI2_17] # xmm12 = [-1.0E+0,-1.0E+0]
	movapd	xmm9, xmmword ptr [rip + .LCPI2_18] # xmm9 = [1.0E+0,1.0E+0]
	mov	rdi, r14
	jmp	.LBB2_2
	.p2align	4
.LBB2_7:                                #   in Loop: Header=BB2_2 Depth=1
	xor	eax, eax
.LBB2_18:                               #   in Loop: Header=BB2_2 Depth=1
	mov	ecx, eax
	shl	ecx, 13
	lea	r14d, [rcx + 4*rax]
	add	r14, r12
	divsd	xmm6, xmm15
	movapd	xmm0, xmm6
	call	floor
	movapd	xmm1, xmm6
	subsd	xmm1, xmm0
	mulsd	xmm1, qword ptr [rip + .LCPI2_13]
	cvttsd2si	rcx, xmm1
	mov	rdx, rcx
	sar	rdx, 63
	movapd	xmm0, xmm1
	subsd	xmm0, qword ptr [rip + .LCPI2_14]
	cvttsd2si	rax, xmm0
	and	rax, rdx
	or	rax, rcx
	lea	ecx, [rax + 1]
	and	ecx, 2047
	movss	xmm0, dword ptr [r14 + 4*rax]   # xmm0 = mem[0],zero,zero,zero
	movss	xmm2, dword ptr [r14 + 4*rcx]   # xmm2 = mem[0],zero,zero,zero
	unpcklps	xmm2, xmm0                      # xmm2 = xmm2[0],xmm0[0],xmm2[1],xmm0[1]
	cvtps2pd	xmm2, xmm2
	movapd	xmm0, xmm2
	andpd	xmm0, xmmword ptr [rip + .LCPI2_15]
	movapd	xmm3, xmm0
	cmpneqpd	xmm3, xmm14
	cmpordpd	xmm0, xmm14
	andpd	xmm0, xmm3
	andpd	xmm0, xmm2
	movapd	xmm2, xmm9
	minpd	xmm2, xmm0
	cmpltpd	xmm0, xmm12
	movapd	xmm3, xmm12
	andpd	xmm3, xmm0
	andnpd	xmm0, xmm2
	orpd	xmm0, xmm3
	pshufd	xmm2, xmm0, 238                 # xmm2 = xmm0[2,3,2,3]
	subsd	xmm0, xmm2
	movq	xmm3, rax
	punpckldq	xmm3, xmmword ptr [rip + .LCPI2_19] # xmm3 = xmm3[0],mem[0],xmm3[1],mem[1]
	subpd	xmm3, xmmword ptr [rip + .LCPI2_20]
	movapd	xmm4, xmm3
	unpckhpd	xmm4, xmm3                      # xmm4 = xmm4[1],xmm3[1]
	addsd	xmm4, xmm3
	subsd	xmm1, xmm4
	mulsd	xmm1, xmm0
	addsd	xmm1, xmm2
	xorps	xmm0, xmm0
	cvtsd2ss	xmm0, xmm1
	movss	dword ptr [rsi + 4*r13 + 312], xmm0
	addsd	xmm7, xmm6
	movapd	xmm0, xmm7
	call	floor
	subsd	xmm7, xmm0
	movapd	xmm6, xmm7
	mulsd	xmm6, xmm15
	inc	r13
	cmp	rbx, r13
	mov	r14, rdi
	je	.LBB2_19
.LBB2_2:                                # =>This Inner Loop Header: Depth=1
	movapd	xmm0, xmm11
	movaps	xmm11, xmm10
	subsd	xmm11, xmm0
	mulsd	xmm11, qword ptr [rip + .LCPI2_0]
	addsd	xmm11, xmm0
	movq	rax, xmm11
	and	rax, r14
	xorpd	xmm7, xmm7
	movapd	xmm0, xmm11
	cmp	rax, rbp
	jge	.LBB2_3
# %bb.4:                                #   in Loop: Header=BB2_2 Depth=1
	ucomisd	xmm7, xmm0
	jbe	.LBB2_5
.LBB2_6:                                #   in Loop: Header=BB2_2 Depth=1
	test	r15d, r15d
	jne	.LBB2_8
	jmp	.LBB2_7
	.p2align	4
.LBB2_3:                                #   in Loop: Header=BB2_2 Depth=1
	xorpd	xmm0, xmm0
	ucomisd	xmm7, xmm0
	ja	.LBB2_6
.LBB2_5:                                #   in Loop: Header=BB2_2 Depth=1
	movapd	xmm7, xmm8
	minsd	xmm7, xmm0
	divsd	xmm7, qword ptr [rip + .LCPI2_2]
	test	r15d, r15d
	je	.LBB2_7
.LBB2_8:                                #   in Loop: Header=BB2_2 Depth=1
	movapd	xmm0, xmm7
	addsd	xmm0, xmm7
	ucomisd	xmm13, xmm0
	mov	eax, 0
	jbe	.LBB2_18
# %bb.9:                                #   in Loop: Header=BB2_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI2_4]
	mov	eax, 1
	ucomisd	xmm13, xmm0
	jbe	.LBB2_18
# %bb.10:                               #   in Loop: Header=BB2_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI2_5]
	mov	eax, 2
	ucomisd	xmm13, xmm0
	jbe	.LBB2_18
# %bb.11:                               #   in Loop: Header=BB2_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI2_6]
	mov	eax, 3
	ucomisd	xmm13, xmm0
	jbe	.LBB2_18
# %bb.12:                               #   in Loop: Header=BB2_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI2_7]
	mov	eax, 4
	ucomisd	xmm13, xmm0
	jbe	.LBB2_18
# %bb.13:                               #   in Loop: Header=BB2_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI2_8]
	mov	eax, 5
	ucomisd	xmm13, xmm0
	jbe	.LBB2_18
# %bb.14:                               #   in Loop: Header=BB2_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI2_9]
	mov	eax, 6
	ucomisd	xmm13, xmm0
	jbe	.LBB2_18
# %bb.15:                               #   in Loop: Header=BB2_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI2_10]
	mov	eax, 7
	ucomisd	xmm13, xmm0
	jbe	.LBB2_18
# %bb.16:                               #   in Loop: Header=BB2_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI2_11]
	mov	eax, 8
	ucomisd	xmm13, xmm0
	jbe	.LBB2_18
# %bb.17:                               #   in Loop: Header=BB2_2 Depth=1
	mov	eax, 9
	jmp	.LBB2_18
.LBB2_19:
	movsd	qword ptr [rsi + 72], xmm11
	movsd	qword ptr [rsi + 56], xmm6
	mov	edi, dword ptr [rsp + 44]       # 4-byte Reload
	test	edi, edi
	jle	.LBB2_26
# %bb.20:
	mov	eax, dword ptr [rsi + 168]
	movzx	ecx, byte ptr [rsi + 172]
	movsd	xmm0, qword ptr [rsi + 152]     # xmm0 = mem[0],zero
	movsd	xmm10, qword ptr [rsi + 160]    # xmm10 = mem[0],zero
	mov	edx, edi
	xor	r8d, r8d
	movaps	xmm1, xmmword ptr [rip + .LCPI2_21] # xmm1 = [4.15799813E-3,4.15799813E-3,u,u]
	mov	r9d, eax
	movss	xmm2, dword ptr [rip + .LCPI2_25] # xmm2 = [1.0E+0,0.0E+0,0.0E+0,0.0E+0]
	movss	xmm3, dword ptr [rip + .LCPI2_26] # xmm3 = [-1.0E+0,0.0E+0,0.0E+0,0.0E+0]
	lea	r10, [rip + .LJTI2_0]
	movaps	xmm4, xmmword ptr [rip + .LCPI2_23] # xmm4 = [NaN,NaN,NaN,NaN]
	movss	xmm5, dword ptr [rip + .LCPI2_24] # xmm5 = [9.99999996E-13,0.0E+0,0.0E+0,0.0E+0]
	movaps	xmm6, xmmword ptr [rip + .LCPI2_22] # xmm6 = [-0.0E+0,-0.0E+0,-0.0E+0,-0.0E+0]
	jmp	.LBB2_21
	.p2align	4
.LBB2_61:                               #   in Loop: Header=BB2_21 Depth=1
	movss	dword ptr [rsi + 4*r8 + 1336], xmm10
	inc	r8
	movaps	xmm10, xmm7
	cmp	rdx, r8
	je	.LBB2_25
.LBB2_21:                               # =>This Inner Loop Header: Depth=1
	movaps	xmm8, xmm0
	subps	xmm8, xmm10
	mulps	xmm8, xmm1
	movaps	xmm9, xmm0
	movaps	xmm7, xmm0
	cmp	edi, 1
	je	.LBB2_23
# %bb.22:                               #   in Loop: Header=BB2_21 Depth=1
	addps	xmm10, xmm8
	movaps	xmm9, xmm10
	movaps	xmm7, xmm10
.LBB2_23:                               #   in Loop: Header=BB2_21 Depth=1
	xorps	xmm10, xmm10
	cmp	eax, 7
	ja	.LBB2_57
# %bb.24:                               #   in Loop: Header=BB2_21 Depth=1
	movss	xmm11, dword ptr [rsi + 4*r8 + 312] # xmm11 = mem[0],zero,zero,zero
	movsxd	r11, dword ptr [r10 + 4*r9]
	add	r11, r10
	jmp	r11
.LBB2_47:                               #   in Loop: Header=BB2_21 Depth=1
	movaps	xmm8, xmm7
	shufps	xmm8, xmm7, 85                  # xmm8 = xmm8[1,1],xmm7[1,1]
	mulss	xmm11, xmm8
	jmp	.LBB2_56
.LBB2_52:                               #   in Loop: Header=BB2_21 Depth=1
	movaps	xmm10, xmm7
	shufps	xmm10, xmm7, 85                 # xmm10 = xmm10[1,1],xmm7[1,1]
	minss	xmm10, xmm11
	jmp	.LBB2_57
.LBB2_49:                               #   in Loop: Header=BB2_21 Depth=1
	movaps	xmm8, xmm7
	shufps	xmm8, xmm7, 85                  # xmm8 = xmm8[1,1],xmm7[1,1]
	subss	xmm11, xmm8
	jmp	.LBB2_56
.LBB2_50:                               #   in Loop: Header=BB2_21 Depth=1
	movaps	xmm8, xmm7
	shufps	xmm8, xmm7, 85                  # xmm8 = xmm8[1,1],xmm7[1,1]
	movaps	xmm12, xmm8
	andps	xmm12, xmm4
	ucomiss	xmm5, xmm12
	ja	.LBB2_57
# %bb.51:                               #   in Loop: Header=BB2_21 Depth=1
	divss	xmm11, xmm8
	jmp	.LBB2_56
.LBB2_55:                               #   in Loop: Header=BB2_21 Depth=1
	xorps	xmm11, xmm6
	jmp	.LBB2_56
.LBB2_48:                               #   in Loop: Header=BB2_21 Depth=1
	movaps	xmm8, xmm7
	shufps	xmm8, xmm7, 85                  # xmm8 = xmm8[1,1],xmm7[1,1]
	addss	xmm11, xmm8
	jmp	.LBB2_56
.LBB2_53:                               #   in Loop: Header=BB2_21 Depth=1
	movaps	xmm10, xmm7
	shufps	xmm10, xmm7, 85                 # xmm10 = xmm10[1,1],xmm7[1,1]
	maxss	xmm10, xmm11
	jmp	.LBB2_57
.LBB2_54:                               #   in Loop: Header=BB2_21 Depth=1
	andps	xmm11, xmm4
	.p2align	4
.LBB2_56:                               #   in Loop: Header=BB2_21 Depth=1
	movaps	xmm10, xmm11
.LBB2_57:                               #   in Loop: Header=BB2_21 Depth=1
	movd	r11d, xmm10
	and	r11d, 2147483647
	cmp	r11d, 2139095040
	jl	.LBB2_59
# %bb.58:                               #   in Loop: Header=BB2_21 Depth=1
	xorps	xmm10, xmm10
.LBB2_59:                               #   in Loop: Header=BB2_21 Depth=1
	test	cl, cl
	je	.LBB2_61
# %bb.60:                               #   in Loop: Header=BB2_21 Depth=1
	movaps	xmm8, xmm2
	minss	xmm8, xmm10
	cmpltss	xmm10, xmm3
	movaps	xmm11, xmm10
	andnps	xmm11, xmm8
	andps	xmm10, xmm3
	orps	xmm10, xmm11
	jmp	.LBB2_61
.LBB2_25:
	movss	dword ptr [rsi + 160], xmm9
	shufps	xmm7, xmm7, 85                  # xmm7 = xmm7[1,1,1,1]
	movss	dword ptr [rsi + 164], xmm7
.LBB2_26:
	movups	xmm1, xmmword ptr [rsi + 232]
	movupd	xmm0, xmmword ptr [rsi + 248]
	movaps	xmm10, xmm1
	shufps	xmm10, xmm1, 245                # xmm10 = xmm10[1,1],xmm1[3,3]
	andps	xmm10, xmmword ptr [rip + .LCPI2_23]
	pcmpgtd	xmm10, xmmword ptr [rip + .LCPI2_27]
	movupd	xmm15, xmmword ptr [rsi + 264]
	movapd	xmm2, xmmword ptr [rip + .LCPI2_28] # xmm2 = [5.0E+0,1.0000000000000001E-1]
	movapd	xmm3, xmmword ptr [rip + .LCPI2_29] # xmm3 = [2.16E+4,3.0E+1]
	minpd	xmm3, xmm1
	cmpltpd	xmm1, xmm2
	andpd	xmm2, xmm1
	andnpd	xmm1, xmm3
	orpd	xmm1, xmm2
	movsd	xmm12, qword ptr [rsi + 280]    # xmm12 = mem[0],zero
	movsd	xmm11, qword ptr [rsi + 288]    # xmm11 = mem[0],zero
	movsd	xmm7, qword ptr [rsi + 296]     # xmm7 = mem[0],zero
	movdqa	xmm2, xmm10
	pandn	xmm2, xmm1
	pand	xmm10, xmmword ptr [rip + .LCPI2_30]
	por	xmm10, xmm2
	xor	edi, edi
	movabs	r15, 9218868437227405311
	movsd	xmm8, qword ptr [rip + .LCPI2_39] # xmm8 = [1.0E+0,0.0E+0]
	jmp	.LBB2_27
	.p2align	4
.LBB2_44:                               #   in Loop: Header=BB2_27 Depth=1
	movzx	eax, al
	movd	xmm3, eax
	pshufd	xmm3, xmm3, 0                   # xmm3 = xmm3[0,0,0,0]
	shufps	xmm15, xmm15, 160               # xmm15 = xmm15[0,0,2,2]
	orps	xmm15, xmm3
	pslld	xmm15, 31
	psrad	xmm15, 31
	pandn	xmm15, xmm0
	movss	dword ptr [rsi + 4*rdi + 2360], xmm2
	inc	rdi
	movapd	xmm0, xmm6
	unpcklpd	xmm0, xmm1                      # xmm0 = xmm0[0],xmm1[0]
	cmp	rbx, rdi
	je	.LBB2_45
.LBB2_27:                               # =>This Inner Loop Header: Depth=1
	movdqa	xmm6, xmm10
	subpd	xmm6, xmm0
	mulpd	xmm6, xmmword ptr [rip + .LCPI2_31]
	addpd	xmm6, xmm0
	ucomisd	xmm6, qword ptr [rip + .LCPI2_32]
	movhpd	qword ptr [rsi + 256], xmm6
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI2_33]
	movsd	xmm1, qword ptr [rip + .LCPI2_34] # xmm1 = [1.4137166941154071E+0,0.0E+0]
	ja	.LBB2_29
# %bb.28:                               #   in Loop: Header=BB2_27 Depth=1
	divsd	xmm0, qword ptr [rip + .LCPI2_2]
	movapd	xmm1, xmm0
.LBB2_29:                               #   in Loop: Header=BB2_27 Depth=1
	movapd	xmm9, xmm6
	unpckhpd	xmm9, xmm6                      # xmm9 = xmm9[1],xmm6[1]
	movapd	xmm0, xmm6
	cmpltsd	xmm0, qword ptr [rip + .LCPI2_35]
	movapd	xmm2, xmm0
	movsd	xmm3, qword ptr [rip + .LCPI2_36] # xmm3 = [3.2724923474893676E-4,0.0E+0]
	andpd	xmm2, xmm3
	andnpd	xmm0, xmm1
	orpd	xmm0, xmm2
	call	tan
	ucomisd	xmm9, qword ptr [rip + .LCPI2_37]
	movsd	xmm1, qword ptr [rsi + 256]     # xmm1 = mem[0],zero
	movsd	xmm3, qword ptr [rip + .LCPI2_38] # xmm3 = [3.3333333333333333E-2,0.0E+0]
	ja	.LBB2_31
# %bb.30:                               #   in Loop: Header=BB2_27 Depth=1
	movapd	xmm3, xmm8
	divsd	xmm3, xmm1
.LBB2_31:                               #   in Loop: Header=BB2_27 Depth=1
	movapd	xmm2, xmm6
	cmpltpd	xmm2, xmmword ptr [rip + .LCPI2_28]
	pextrw	eax, xmm2, 4
	movsd	xmm2, qword ptr [rip + .LCPI2_40] # xmm2 = [1.0E+1,0.0E+0]
	test	al, 1
	jne	.LBB2_33
# %bb.32:                               #   in Loop: Header=BB2_27 Depth=1
	movapd	xmm2, xmm3
.LBB2_33:                               #   in Loop: Header=BB2_27 Depth=1
	movd	xmm3, dword ptr [rsi + 4*rdi + 1336] # xmm3 = mem[0],zero,zero,zero
	movd	eax, xmm3
	and	eax, 2147483647
	cmp	eax, 2139095040
	jl	.LBB2_34
# %bb.35:                               #   in Loop: Header=BB2_27 Depth=1
	xorps	xmm3, xmm3
	mov	eax, dword ptr [rsi + 304]
	movapd	xmm9, xmm8
	test	eax, eax
	je	.LBB2_38
.LBB2_37:                               #   in Loop: Header=BB2_27 Depth=1
	xorpd	xmm9, xmm9
.LBB2_38:                               #   in Loop: Header=BB2_27 Depth=1
	movapd	xmm13, xmm8
	cmp	eax, 1
	je	.LBB2_40
# %bb.39:                               #   in Loop: Header=BB2_27 Depth=1
	xorpd	xmm13, xmm13
.LBB2_40:                               #   in Loop: Header=BB2_27 Depth=1
	movapd	xmm4, xmm0
	addsd	xmm4, xmm2
	mulsd	xmm4, xmm0
	addsd	xmm4, xmm8
	movapd	xmm14, xmm15
	unpckhpd	xmm14, xmm15                    # xmm14 = xmm14[1],xmm15[1]
	movaps	xmm5, xmm3
	subsd	xmm5, xmm14
	subsd	xmm9, xmm12
	movsd	xmm14, qword ptr [rip + .LCPI2_0] # xmm14 = [4.1579981548900413E-3,0.0E+0]
	mulsd	xmm9, xmm14
	addsd	xmm12, xmm9
	subsd	xmm13, xmm11
	mulsd	xmm13, xmm14
	addsd	xmm11, xmm13
	movapd	xmm9, xmm8
	cmp	eax, 2
	je	.LBB2_42
# %bb.41:                               #   in Loop: Header=BB2_27 Depth=1
	xorpd	xmm9, xmm9
.LBB2_42:                               #   in Loop: Header=BB2_27 Depth=1
	subsd	xmm9, xmm7
	mulsd	xmm9, qword ptr [rip + .LCPI2_0]
	addsd	xmm7, xmm9
	movapd	xmm9, xmm8
	divsd	xmm9, xmm4
	movapd	xmm4, xmm0
	mulsd	xmm4, xmm9
	movapd	xmm13, xmm15
	mulsd	xmm13, xmm4
	movapd	xmm14, xmm15
	mulpd	xmm14, xmm9
	unpcklpd	xmm9, xmm13                     # xmm9 = xmm9[0],xmm13[0]
	addpd	xmm9, xmm15
	movsd	xmm9, xmm14                     # xmm9 = xmm14[0],xmm9[1]
	mulsd	xmm0, xmm4
	unpcklpd	xmm4, xmm0                      # xmm4 = xmm4[0],xmm0[0]
	unpcklpd	xmm5, xmm5                      # xmm5 = xmm5[0,0]
	mulpd	xmm4, xmm5
	addpd	xmm4, xmm9
	movapd	xmm0, xmm4
	addpd	xmm0, xmm4
	subpd	xmm0, xmm15
	mulsd	xmm2, xmm4
	subsd	xmm3, xmm2
	movapd	xmm2, xmm4
	unpckhpd	xmm2, xmm4                      # xmm2 = xmm2[1],xmm4[1]
	subsd	xmm3, xmm2
	mulsd	xmm2, xmm12
	mulsd	xmm3, xmm11
	addsd	xmm3, xmm2
	mulsd	xmm4, xmm7
	addsd	xmm4, xmm3
	movq	rax, xmm4
	movapd	xmm2, xmm4
	movapd	xmm3, xmmword ptr [rip + .LCPI2_15] # xmm3 = [NaN,NaN]
	andpd	xmm2, xmm3
	and	rax, r14
	cmp	rax, r15
	setg	al
	ucomisd	xmm2, qword ptr [rip + .LCPI2_41]
	seta	cl
	movapd	xmm15, xmm0
	andpd	xmm15, xmm3
	movsd	xmm3, qword ptr [rip + .LCPI2_42] # xmm3 = [+Inf,0.0E+0]
	ucomisd	xmm15, xmm3
	sete	dl
	or	dl, al
	pshufd	xmm2, xmm15, 238                # xmm2 = xmm15[2,3,2,3]
	ucomisd	xmm2, xmm3
	sete	al
	or	al, dl
	or	al, cl
	cmpltpd	xmm15, xmmword ptr [rip + .LCPI2_43]
	xorpd	xmm2, xmm2
	jne	.LBB2_44
# %bb.43:                               #   in Loop: Header=BB2_27 Depth=1
	xorps	xmm2, xmm2
	cvtsd2ss	xmm2, xmm4
	jmp	.LBB2_44
	.p2align	4
.LBB2_34:                               #   in Loop: Header=BB2_27 Depth=1
	cvtss2sd	xmm3, xmm3
	mov	eax, dword ptr [rsi + 304]
	movapd	xmm9, xmm8
	test	eax, eax
	jne	.LBB2_37
	jmp	.LBB2_38
.LBB2_45:
	movsd	qword ptr [rsi + 248], xmm6
	movsd	qword ptr [rsi + 280], xmm12
	movsd	qword ptr [rsi + 288], xmm11
	movsd	qword ptr [rsi + 296], xmm7
	movupd	xmmword ptr [rsi + 264], xmm15
.LBB2_46:
	movaps	xmm6, xmmword ptr [rsp + 48]    # 16-byte Reload
	movaps	xmm7, xmmword ptr [rsp + 64]    # 16-byte Reload
	movaps	xmm8, xmmword ptr [rsp + 80]    # 16-byte Reload
	movaps	xmm9, xmmword ptr [rsp + 96]    # 16-byte Reload
	movaps	xmm10, xmmword ptr [rsp + 112]  # 16-byte Reload
	movaps	xmm11, xmmword ptr [rsp + 128]  # 16-byte Reload
	movaps	xmm12, xmmword ptr [rsp + 144]  # 16-byte Reload
	movaps	xmm13, xmmword ptr [rsp + 160]  # 16-byte Reload
	movaps	xmm14, xmmword ptr [rsp + 176]  # 16-byte Reload
	movaps	xmm15, xmmword ptr [rsp + 192]  # 16-byte Reload
	add	rsp, 216
	pop	rbx
	pop	rbp
	pop	rdi
	pop	rsi
	pop	r12
	pop	r13
	pop	r14
	pop	r15
	ret
	.section	.rdata,"dr",associative,_Z12block_kernelILb1EEvR5Chaini
	.p2align	2, 0x0
.LJTI2_0:
	.long	.LBB2_47-.LJTI2_0
	.long	.LBB2_48-.LJTI2_0
	.long	.LBB2_49-.LJTI2_0
	.long	.LBB2_50-.LJTI2_0
	.long	.LBB2_52-.LJTI2_0
	.long	.LBB2_53-.LJTI2_0
	.long	.LBB2_54-.LJTI2_0
	.long	.LBB2_55-.LJTI2_0
	.section	.text$_Z12block_kernelILb1EEvR5Chaini,"xr",discard,_Z12block_kernelILb1EEvR5Chaini
	.seh_endproc
                                        # -- End function
	.def	fused_chain;
	.scl	2;
	.type	32;
	.endef
	.text
	.globl	fused_chain                     # -- Begin function fused_chain
	.p2align	4
fused_chain:                            # @fused_chain
# %bb.0:
	jmp	_Z12sample_chainILb1EEvR5Chaini # TAILCALL
                                        # -- End function
	.def	_Z12sample_chainILb1EEvR5Chaini;
	.scl	2;
	.type	32;
	.endef
	.section	.rdata,"dr"
	.p2align	4, 0x0                          # -- Begin function _Z12sample_chainILb1EEvR5Chaini
.LCPI4_0:
	.long	2147483647                      # 0x7fffffff
	.long	2147483647                      # 0x7fffffff
	.long	2147483647                      # 0x7fffffff
	.long	2147483647                      # 0x7fffffff
.LCPI4_1:
	.long	2146435071                      # 0x7fefffff
	.long	2146435071                      # 0x7fefffff
	.long	2146435071                      # 0x7fefffff
	.long	2146435071                      # 0x7fefffff
.LCPI4_2:
	.quad	0x4014000000000000              # double 5
	.quad	0x3fb999999999999a              # double 0.10000000000000001
.LCPI4_3:
	.quad	0x40d5180000000000              # double 21600
	.quad	0x403e000000000000              # double 30
.LCPI4_4:
	.quad	0x408f400000000000              # double 1000
	.quad	0x3fe69fbe76c8b439              # double 0.70699999999999996
.LCPI4_5:
	.quad	0x3f7107fa21792a00              # double 0.0041579981548900413
.LCPI4_6:
	.quad	0x40d3880000000000              # double 2.0E+4
.LCPI4_7:
	.quad	0x40e7700000000000              # double 48000
.LCPI4_8:
	.quad	0x3fe0000000000000              # double 0.5
.LCPI4_9:
	.quad	0x4010000000000000              # double 4
.LCPI4_10:
	.quad	0x4020000000000000              # double 8
.LCPI4_11:
	.quad	0x4030000000000000              # double 16
.LCPI4_12:
	.quad	0x4040000000000000              # double 32
.LCPI4_13:
	.quad	0x4050000000000000              # double 64
.LCPI4_14:
	.quad	0x4060000000000000              # double 128
.LCPI4_15:
	.quad	0x4070000000000000              # double 256
.LCPI4_16:
	.quad	0x4080000000000000              # double 512
.LCPI4_17:
	.quad	0x401921fb54442d18              # double 6.2831853071795862
.LCPI4_18:
	.quad	0x40a0000000000000              # double 2048
.LCPI4_19:
	.quad	0x43e0000000000000              # double 9.2233720368547758E+18
.LCPI4_20:
	.quad	0x3ff0000000000000              # double 1
.LCPI4_21:
	.quad	0xbff0000000000000              # double -1
	.zero	8
.LCPI4_22:
	.long	1127219200                      # 0x43300000
	.long	1160773632                      # 0x45300000
	.long	0                               # 0x0
	.long	0                               # 0x0
.LCPI4_23:
	.quad	0x4330000000000000              # double 4503599627370496
	.quad	0x4530000000000000              # double 1.9342813113834067E+25
.LCPI4_24:
	.long	0x3b883fd1                      # float 0.00415799813
	.long	0x3b883fd1                      # float 0.00415799813
	.zero	4
	.zero	4
.LCPI4_25:
	.long	0x80000000                      # float -0
	.long	0x80000000                      # float -0
	.long	0x80000000                      # float -0
	.long	0x80000000                      # float -0
.LCPI4_26:
	.long	0x2b8cbccc                      # float 9.99999996E-13
.LCPI4_27:
	.long	0x3f800000                      # float 1
.LCPI4_28:
	.long	0xbf800000                      # float -1
	.zero	4
.LCPI4_29:
	.quad	0x3f7107fa21792a00              # double 0.0041579981548900413
	.quad	0x3f7107fa21792a00              # double 0.0041579981548900413
.LCPI4_30:
	.quad	0x40d5180000000000              # double 21600
.LCPI4_31:
	.quad	0x400921fb54442d18              # double 3.1415926535897931
.LCPI4_32:
	.quad	0x3ff69e9565708efd              # double 1.4137166941154071
.LCPI4_33:
	.quad	0x4014000000000000              # double 5
.LCPI4_34:
	.quad	0x3f357254c2c98977              # double 3.2724923474893676E-4
.LCPI4_35:
	.quad	0x403e000000000000              # double 30
.LCPI4_36:
	.quad	0x3fa1111111111111              # double 0.033333333333333333
.LCPI4_37:
	.quad	0x4024000000000000              # double 10
.LCPI4_38:
	.quad	0x7fffffffffffffff              # double NaN
	.quad	0x7fffffffffffffff              # double NaN
.LCPI4_39:
	.quad	0x47efffffe0000000              # double 3.4028234663852886E+38
.LCPI4_40:
	.quad	0x7ff0000000000000              # double +Inf
.LCPI4_41:
	.quad	0x3abef2d0f5da7dd9              # double 1.0E-25
	.quad	0x3abef2d0f5da7dd9              # double 1.0E-25
	.section	.text$_Z12sample_chainILb1EEvR5Chaini,"xr",discard,_Z12sample_chainILb1EEvR5Chaini
	.globl	_Z12sample_chainILb1EEvR5Chaini
	.p2align	4
_Z12sample_chainILb1EEvR5Chaini:        # @_Z12sample_chainILb1EEvR5Chaini
.seh_proc _Z12sample_chainILb1EEvR5Chaini
# %bb.0:
	push	r15
	.seh_pushreg r15
	push	r14
	.seh_pushreg r14
	push	r13
	.seh_pushreg r13
	push	r12
	.seh_pushreg r12
	push	rsi
	.seh_pushreg rsi
	push	rdi
	.seh_pushreg rdi
	push	rbp
	.seh_pushreg rbp
	push	rbx
	.seh_pushreg rbx
	sub	rsp, 328
	.seh_stackalloc 328
	movapd	xmmword ptr [rsp + 304], xmm15  # 16-byte Spill
	.seh_savexmm xmm15, 304
	movapd	xmmword ptr [rsp + 288], xmm14  # 16-byte Spill
	.seh_savexmm xmm14, 288
	movapd	xmmword ptr [rsp + 272], xmm13  # 16-byte Spill
	.seh_savexmm xmm13, 272
	movapd	xmmword ptr [rsp + 256], xmm12  # 16-byte Spill
	.seh_savexmm xmm12, 256
	movaps	xmmword ptr [rsp + 240], xmm11  # 16-byte Spill
	.seh_savexmm xmm11, 240
	movapd	xmmword ptr [rsp + 224], xmm10  # 16-byte Spill
	.seh_savexmm xmm10, 224
	movapd	xmmword ptr [rsp + 208], xmm9   # 16-byte Spill
	.seh_savexmm xmm9, 208
	movapd	xmmword ptr [rsp + 192], xmm8   # 16-byte Spill
	.seh_savexmm xmm8, 192
	movapd	xmmword ptr [rsp + 176], xmm7   # 16-byte Spill
	.seh_savexmm xmm7, 176
	movapd	xmmword ptr [rsp + 160], xmm6   # 16-byte Spill
	.seh_savexmm xmm6, 160
	.seh_endprologue
	test	edx, edx
	jle	.LBB4_9
# %bb.1:
	mov	edi, edx
	mov	rsi, rcx
	movss	xmm0, dword ptr [rcx + 64]      # xmm0 = mem[0],zero,zero,zero
	cvtss2sd	xmm0, xmm0
	movsd	qword ptr [rsp + 72], xmm0      # 8-byte Spill
	mov	rax, qword ptr [rcx + 88]
	mov	qword ptr [rsp + 64], rax       # 8-byte Spill
	mov	eax, dword ptr [rcx + 168]
	movups	xmm0, xmmword ptr [rcx + 232]
	movaps	xmm3, xmm0
	shufps	xmm3, xmm0, 245                 # xmm3 = xmm3[1,1],xmm0[3,3]
	andps	xmm3, xmmword ptr [rip + .LCPI4_0]
	pcmpgtd	xmm3, xmmword ptr [rip + .LCPI4_1]
	movzx	ecx, byte ptr [rcx + 172]
	mov	byte ptr [rsp + 47], cl         # 1-byte Spill
	movapd	xmm1, xmmword ptr [rip + .LCPI4_2] # xmm1 = [5.0E+0,1.0000000000000001E-1]
	movapd	xmm2, xmmword ptr [rip + .LCPI4_3] # xmm2 = [2.16E+4,3.0E+1]
	minpd	xmm2, xmm0
	cmpltpd	xmm0, xmm1
	andpd	xmm1, xmm0
	andnpd	xmm0, xmm2
	orpd	xmm0, xmm1
	movsd	xmm10, qword ptr [rsi + 72]     # xmm10 = mem[0],zero
	movsd	xmm6, qword ptr [rsi + 56]      # xmm6 = mem[0],zero
	movsd	xmm1, qword ptr [rsi + 152]     # xmm1 = mem[0],zero
	movaps	xmmword ptr [rsp + 112], xmm1   # 16-byte Spill
	movsd	xmm11, qword ptr [rsi + 160]    # xmm11 = mem[0],zero
	movupd	xmm9, xmmword ptr [rsi + 264]
	mov	ecx, edx
	mov	qword ptr [rsp + 56], rcx       # 8-byte Spill
	movdqa	xmm1, xmm3
	pandn	xmm1, xmm0
	pand	xmm3, xmmword ptr [rip + .LCPI4_4]
	por	xmm3, xmm1
	movdqa	xmmword ptr [rsp + 128], xmm3   # 16-byte Spill
	movsd	xmm15, qword ptr [rsi + 248]    # xmm15 = mem[0],zero
	movsd	xmm14, qword ptr [rsi + 280]    # xmm14 = mem[0],zero
	movsd	xmm13, qword ptr [rsi + 288]    # xmm13 = mem[0],zero
	movsd	xmm7, qword ptr [rsi + 296]     # xmm7 = mem[0],zero
	xor	r12d, r12d
	movabs	r13, 9223372036854775807
	movabs	rbx, 9218868437227405312
	movsd	xmm1, qword ptr [rip + .LCPI4_8] # xmm1 = [5.0E-1,0.0E+0]
	mov	r15d, eax
	mov	eax, eax
	mov	qword ptr [rsp + 48], rax       # 8-byte Spill
	movsd	xmm3, qword ptr [rip + .LCPI4_17] # xmm3 = [6.2831853071795862E+0,0.0E+0]
	jmp	.LBB4_2
	.p2align	4
.LBB4_59:                               #   in Loop: Header=BB4_2 Depth=1
	subsd	xmm12, qword ptr [rsp + 80]     # 8-byte Folded Reload
	movapd	xmm6, xmm12
	movsd	xmm3, qword ptr [rip + .LCPI4_17] # xmm3 = [6.2831853071795862E+0,0.0E+0]
	mulsd	xmm6, xmm3
	movzx	eax, al
	movd	xmm2, eax
	pshufd	xmm2, xmm2, 0                   # xmm2 = xmm2[0,0,0,0]
	shufps	xmm9, xmm9, 160                 # xmm9 = xmm9[0,0,2,2]
	orps	xmm9, xmm2
	pslld	xmm9, 31
	psrad	xmm9, 31
	pandn	xmm9, xmm0
	movss	dword ptr [rsi + 4*r12 + 2360], xmm1
	inc	r12
	movaps	xmm11, xmmword ptr [rsp + 144]  # 16-byte Reload
	cmp	qword ptr [rsp + 56], r12       # 8-byte Folded Reload
	movsd	xmm1, qword ptr [rip + .LCPI4_8] # xmm1 = [5.0E-1,0.0E+0]
	je	.LBB4_8
.LBB4_2:                                # =>This Inner Loop Header: Depth=1
	movapd	xmm0, xmm10
	movsd	xmm10, qword ptr [rsp + 72]     # 8-byte Reload
                                        # xmm10 = mem[0],zero
	subsd	xmm10, xmm0
	mulsd	xmm10, qword ptr [rip + .LCPI4_5]
	addsd	xmm10, xmm0
	movq	rax, xmm10
	and	rax, r13
	xorpd	xmm12, xmm12
	movapd	xmm0, xmm10
	cmp	rax, rbx
	jl	.LBB4_4
# %bb.3:                                #   in Loop: Header=BB4_2 Depth=1
	xorpd	xmm0, xmm0
.LBB4_4:                                #   in Loop: Header=BB4_2 Depth=1
	ucomisd	xmm12, xmm0
	ja	.LBB4_6
# %bb.5:                                #   in Loop: Header=BB4_2 Depth=1
	movsd	xmm12, qword ptr [rip + .LCPI4_6] # xmm12 = [2.0E+4,0.0E+0]
	minsd	xmm12, xmm0
	divsd	xmm12, qword ptr [rip + .LCPI4_7]
.LBB4_6:                                #   in Loop: Header=BB4_2 Depth=1
	mov	eax, dword ptr [rsi + 80]
	test	rax, rax
	movsd	qword ptr [rsp + 104], xmm7     # 8-byte Spill
	movsd	qword ptr [rsp + 96], xmm13     # 8-byte Spill
	je	.LBB4_7
# %bb.10:                               #   in Loop: Header=BB4_2 Depth=1
	movapd	xmm0, xmm12
	addsd	xmm0, xmm12
	ucomisd	xmm1, xmm0
	mov	ecx, 0
	jbe	.LBB4_20
# %bb.11:                               #   in Loop: Header=BB4_2 Depth=1
	movapd	xmm0, xmm12
	mulsd	xmm0, qword ptr [rip + .LCPI4_9]
	mov	ecx, 1
	ucomisd	xmm1, xmm0
	jbe	.LBB4_20
# %bb.12:                               #   in Loop: Header=BB4_2 Depth=1
	movapd	xmm0, xmm12
	mulsd	xmm0, qword ptr [rip + .LCPI4_10]
	mov	ecx, 2
	ucomisd	xmm1, xmm0
	jbe	.LBB4_20
# %bb.13:                               #   in Loop: Header=BB4_2 Depth=1
	movapd	xmm0, xmm12
	mulsd	xmm0, qword ptr [rip + .LCPI4_11]
	mov	ecx, 3
	ucomisd	xmm1, xmm0
	jbe	.LBB4_20
# %bb.14:                               #   in Loop: Header=BB4_2 Depth=1
	movapd	xmm0, xmm12
	mulsd	xmm0, qword ptr [rip + .LCPI4_12]
	mov	ecx, 4
	ucomisd	xmm1, xmm0
	jbe	.LBB4_20
# %bb.15:                               #   in Loop: Header=BB4_2 Depth=1
	movapd	xmm0, xmm12
	mulsd	xmm0, qword ptr [rip + .LCPI4_13]
	mov	ecx, 5
	ucomisd	xmm1, xmm0
	jbe	.LBB4_20
# %bb.16:                               #   in Loop: Header=BB4_2 Depth=1
	movapd	xmm0, xmm12
	mulsd	xmm0, qword ptr [rip + .LCPI4_14]
	mov	ecx, 6
	ucomisd	xmm1, xmm0
	jbe	.LBB4_20
# %bb.17:                               #   in Loop: Header=BB4_2 Depth=1
	movapd	xmm0, xmm12
	mulsd	xmm0, qword ptr [rip + .LCPI4_15]
	mov	ecx, 7
	ucomisd	xmm1, xmm0
	jbe	.LBB4_20
# %bb.18:                               #   in Loop: Header=BB4_2 Depth=1
	movapd	xmm0, xmm12
	mulsd	xmm0, qword ptr [rip + .LCPI4_16]
	mov	ecx, 8
	ucomisd	xmm1, xmm0
	jbe	.LBB4_20
# %bb.19:                               #   in Loop: Header=BB4_2 Depth=1
	mov	ecx, 9
	jmp	.LBB4_20
	.p2align	4
.LBB4_7:                                #   in Loop: Header=BB4_2 Depth=1
	xor	ecx, ecx
.LBB4_20:                               #   in Loop: Header=BB4_2 Depth=1
	movsd	qword ptr [rsp + 88], xmm14     # 8-byte Spill
	imul	rax, rax, 81960
	mov	edx, ecx
	shl	edx, 13
	lea	r14d, [rdx + 4*rcx]
	add	r14, qword ptr [rsp + 64]       # 8-byte Folded Reload
	add	r14, rax
	divsd	xmm6, xmm3
	movapd	xmm0, xmm6
	call	floor
	movapd	xmm7, xmm6
	subsd	xmm7, xmm0
	mulsd	xmm7, qword ptr [rip + .LCPI4_18]
	cvttsd2si	rax, xmm7
	mov	rcx, rax
	sar	rcx, 63
	movapd	xmm0, xmm7
	subsd	xmm0, qword ptr [rip + .LCPI4_19]
	cvttsd2si	rbp, xmm0
	and	rbp, rcx
	or	rbp, rax
	movss	xmm0, dword ptr [r14 + 4*rbp]   # xmm0 = mem[0],zero,zero,zero
	xorps	xmm13, xmm13
	cvtss2sd	xmm13, xmm0
	movq	rax, xmm13
	and	rax, r13
	cmp	rax, rbx
	jl	.LBB4_22
# %bb.21:                               #   in Loop: Header=BB4_2 Depth=1
	xorps	xmm13, xmm13
.LBB4_22:                               #   in Loop: Header=BB4_2 Depth=1
	lea	eax, [rbp + 1]
	and	eax, 2047
	movss	xmm0, dword ptr [r14 + 4*rax]   # xmm0 = mem[0],zero,zero,zero
	xorps	xmm14, xmm14
	cvtss2sd	xmm14, xmm0
	movq	rax, xmm14
	and	rax, r13
	cmp	rax, rbx
	jl	.LBB4_24
# %bb.23:                               #   in Loop: Header=BB4_2 Depth=1
	xorps	xmm14, xmm14
.LBB4_24:                               #   in Loop: Header=BB4_2 Depth=1
	addsd	xmm12, xmm6
	movapd	xmm0, xmm12
	call	floor
	movsd	qword ptr [rsp + 80], xmm0      # 8-byte Spill
	movq	xmm2, rbp
	punpckldq	xmm2, xmmword ptr [rip + .LCPI4_22] # xmm2 = xmm2[0],mem[0],xmm2[1],mem[1]
	subpd	xmm2, xmmword ptr [rip + .LCPI4_23]
	movaps	xmm8, xmmword ptr [rsp + 112]   # 16-byte Reload
	movaps	xmm0, xmm8
	subps	xmm0, xmm11
	mulps	xmm0, xmmword ptr [rip + .LCPI4_24]
	movaps	xmm1, xmm8
	cmp	edi, 1
	je	.LBB4_26
# %bb.25:                               #   in Loop: Header=BB4_2 Depth=1
	addps	xmm11, xmm0
	movaps	xmm1, xmm11
	movaps	xmm8, xmm11
.LBB4_26:                               #   in Loop: Header=BB4_2 Depth=1
	movaps	xmm0, xmm8
	shufps	xmm0, xmm8, 85                  # xmm0 = xmm0[1,1],xmm8[1,1]
	movss	dword ptr [rsi + 160], xmm1
	movss	dword ptr [rsi + 164], xmm0
	xorps	xmm1, xmm1
	cmp	r15d, 7
	ja	.LBB4_38
# %bb.27:                               #   in Loop: Header=BB4_2 Depth=1
	movsd	xmm4, qword ptr [rip + .LCPI4_20] # xmm4 = [1.0E+0,0.0E+0]
	movapd	xmm1, xmm4
	minsd	xmm1, xmm13
	movsd	xmm5, qword ptr [rip + .LCPI4_21] # xmm5 = [-1.0E+0,0.0E+0]
	cmpltsd	xmm13, xmm5
	movapd	xmm3, xmm13
	andnpd	xmm3, xmm1
	andpd	xmm13, xmm5
	orpd	xmm13, xmm3
	movapd	xmm1, xmm4
	minsd	xmm1, xmm14
	cmpltsd	xmm14, xmm5
	movapd	xmm3, xmm14
	andnpd	xmm3, xmm1
	andpd	xmm14, xmm5
	orpd	xmm14, xmm3
	subsd	xmm14, xmm13
	movapd	xmm1, xmm2
	unpckhpd	xmm1, xmm2                      # xmm1 = xmm1[1],xmm2[1]
	addsd	xmm1, xmm2
	subsd	xmm7, xmm1
	mulsd	xmm7, xmm14
	addsd	xmm7, xmm13
	xorps	xmm2, xmm2
	cvtsd2ss	xmm2, xmm7
	mov	rax, qword ptr [rsp + 48]       # 8-byte Reload
	lea	rcx, [rip + .LJTI4_0]
	movsxd	rax, dword ptr [rcx + 4*rax]
	add	rax, rcx
	jmp	rax
.LBB4_28:                               #   in Loop: Header=BB4_2 Depth=1
	mulss	xmm0, xmm2
	movaps	xmm1, xmm0
	jmp	.LBB4_38
.LBB4_33:                               #   in Loop: Header=BB4_2 Depth=1
	minss	xmm0, xmm2
	movaps	xmm1, xmm0
	jmp	.LBB4_38
.LBB4_30:                               #   in Loop: Header=BB4_2 Depth=1
	subss	xmm2, xmm0
	jmp	.LBB4_37
.LBB4_31:                               #   in Loop: Header=BB4_2 Depth=1
	movaps	xmm3, xmm0
	andps	xmm3, xmmword ptr [rip + .LCPI4_0]
	xorpd	xmm1, xmm1
	movss	xmm4, dword ptr [rip + .LCPI4_26] # xmm4 = [9.99999996E-13,0.0E+0,0.0E+0,0.0E+0]
	ucomiss	xmm4, xmm3
	ja	.LBB4_38
# %bb.32:                               #   in Loop: Header=BB4_2 Depth=1
	divss	xmm2, xmm0
	jmp	.LBB4_37
.LBB4_36:                               #   in Loop: Header=BB4_2 Depth=1
	xorps	xmm2, xmmword ptr [rip + .LCPI4_25]
	jmp	.LBB4_37
.LBB4_29:                               #   in Loop: Header=BB4_2 Depth=1
	addss	xmm0, xmm2
	movaps	xmm1, xmm0
	jmp	.LBB4_38
.LBB4_34:                               #   in Loop: Header=BB4_2 Depth=1
	maxss	xmm0, xmm2
	movaps	xmm1, xmm0
	jmp	.LBB4_38
.LBB4_35:                               #   in Loop: Header=BB4_2 Depth=1
	andps	xmm2, xmmword ptr [rip + .LCPI4_0]
.LBB4_37:                               #   in Loop: Header=BB4_2 Depth=1
	movaps	xmm1, xmm2
.LBB4_38:                               #   in Loop: Header=BB4_2 Depth=1
	movd	eax, xmm1
	and	eax, 2147483647
	cmp	eax, 2139095040
	jl	.LBB4_40
# %bb.39:                               #   in Loop: Header=BB4_2 Depth=1
	xorps	xmm1, xmm1
.LBB4_40:                               #   in Loop: Header=BB4_2 Depth=1
	cmp	byte ptr [rsp + 47], 0          # 1-byte Folded Reload
	movsd	xmm7, qword ptr [rsp + 104]     # 8-byte Reload
                                        # xmm7 = mem[0],zero
	movsd	xmm13, qword ptr [rsp + 96]     # 8-byte Reload
                                        # xmm13 = mem[0],zero
	movsd	xmm14, qword ptr [rsp + 88]     # 8-byte Reload
                                        # xmm14 = mem[0],zero
	je	.LBB4_42
# %bb.41:                               #   in Loop: Header=BB4_2 Depth=1
	movss	xmm0, dword ptr [rip + .LCPI4_27] # xmm0 = [1.0E+0,0.0E+0,0.0E+0,0.0E+0]
	minss	xmm0, xmm1
	movss	xmm3, dword ptr [rip + .LCPI4_28] # xmm3 = [-1.0E+0,0.0E+0,0.0E+0,0.0E+0]
	cmpltss	xmm1, xmm3
	movaps	xmm2, xmm1
	andnps	xmm2, xmm0
	andps	xmm1, xmm3
	orps	xmm1, xmm2
.LBB4_42:                               #   in Loop: Header=BB4_2 Depth=1
	movd	eax, xmm1
	and	eax, 2147483647
	cmp	eax, 2139095040
	jl	.LBB4_43
# %bb.44:                               #   in Loop: Header=BB4_2 Depth=1
	xorps	xmm11, xmm11
	jmp	.LBB4_45
	.p2align	4
.LBB4_43:                               #   in Loop: Header=BB4_2 Depth=1
	xorps	xmm11, xmm11
	cvtss2sd	xmm11, xmm1
.LBB4_45:                               #   in Loop: Header=BB4_2 Depth=1
	movaps	xmmword ptr [rsp + 144], xmm8   # 16-byte Spill
	movapd	xmm0, xmm15
	movhpd	xmm0, qword ptr [rsi + 256]     # xmm0 = xmm0[0],mem[0]
	movapd	xmm15, xmmword ptr [rsp + 128]  # 16-byte Reload
	subpd	xmm15, xmm0
	mulpd	xmm15, xmmword ptr [rip + .LCPI4_29]
	addpd	xmm15, xmm0
	ucomisd	xmm15, qword ptr [rip + .LCPI4_30]
	movhpd	qword ptr [rsi + 256], xmm15
	movapd	xmm0, xmm15
	mulsd	xmm0, qword ptr [rip + .LCPI4_31]
	movsd	xmm1, qword ptr [rip + .LCPI4_32] # xmm1 = [1.4137166941154071E+0,0.0E+0]
	ja	.LBB4_47
# %bb.46:                               #   in Loop: Header=BB4_2 Depth=1
	divsd	xmm0, qword ptr [rip + .LCPI4_7]
	movapd	xmm1, xmm0
.LBB4_47:                               #   in Loop: Header=BB4_2 Depth=1
	movapd	xmm6, xmm15
	unpckhpd	xmm6, xmm15                     # xmm6 = xmm6[1],xmm15[1]
	movapd	xmm0, xmm15
	cmpltsd	xmm0, qword ptr [rip + .LCPI4_33]
	movapd	xmm2, xmm0
	movsd	xmm3, qword ptr [rip + .LCPI4_34] # xmm3 = [3.2724923474893676E-4,0.0E+0]
	andpd	xmm2, xmm3
	andnpd	xmm0, xmm1
	orpd	xmm0, xmm2
	call	tan
	ucomisd	xmm6, qword ptr [rip + .LCPI4_35]
	movsd	xmm2, qword ptr [rip + .LCPI4_36] # xmm2 = [3.3333333333333333E-2,0.0E+0]
	ja	.LBB4_49
# %bb.48:                               #   in Loop: Header=BB4_2 Depth=1
	movsd	xmm2, qword ptr [rip + .LCPI4_20] # xmm2 = [1.0E+0,0.0E+0]
	divsd	xmm2, qword ptr [rsi + 256]
.LBB4_49:                               #   in Loop: Header=BB4_2 Depth=1
	movapd	xmm1, xmm15
	cmpltpd	xmm1, xmmword ptr [rip + .LCPI4_2]
	pextrw	eax, xmm1, 4
	movsd	xmm1, qword ptr [rip + .LCPI4_37] # xmm1 = [1.0E+1,0.0E+0]
	test	al, 1
	je	.LBB4_50
# %bb.51:                               #   in Loop: Header=BB4_2 Depth=1
	mov	eax, dword ptr [rsi + 304]
	movsd	xmm4, qword ptr [rip + .LCPI4_20] # xmm4 = [1.0E+0,0.0E+0]
	test	eax, eax
	jne	.LBB4_52
.LBB4_53:                               #   in Loop: Header=BB4_2 Depth=1
	movsd	xmm5, qword ptr [rip + .LCPI4_20] # xmm5 = [1.0E+0,0.0E+0]
	cmp	eax, 1
	je	.LBB4_55
.LBB4_54:                               #   in Loop: Header=BB4_2 Depth=1
	xorpd	xmm5, xmm5
.LBB4_55:                               #   in Loop: Header=BB4_2 Depth=1
	movapd	xmm2, xmm0
	addsd	xmm2, xmm1
	mulsd	xmm2, xmm0
	movsd	xmm8, qword ptr [rip + .LCPI4_20] # xmm8 = [1.0E+0,0.0E+0]
	addsd	xmm2, xmm8
	movapd	xmm6, xmm9
	unpckhpd	xmm6, xmm9                      # xmm6 = xmm6[1],xmm9[1]
	movaps	xmm3, xmm11
	subsd	xmm3, xmm6
	subsd	xmm4, xmm14
	movsd	xmm6, qword ptr [rip + .LCPI4_5] # xmm6 = [4.1579981548900413E-3,0.0E+0]
	mulsd	xmm4, xmm6
	addsd	xmm14, xmm4
	subsd	xmm5, xmm13
	mulsd	xmm5, xmm6
	addsd	xmm13, xmm5
	cmp	eax, 2
	je	.LBB4_57
# %bb.56:                               #   in Loop: Header=BB4_2 Depth=1
	xorpd	xmm8, xmm8
.LBB4_57:                               #   in Loop: Header=BB4_2 Depth=1
	subsd	xmm8, xmm7
	mulsd	xmm8, qword ptr [rip + .LCPI4_5]
	addsd	xmm7, xmm8
	movsd	xmm4, qword ptr [rip + .LCPI4_20] # xmm4 = [1.0E+0,0.0E+0]
	divsd	xmm4, xmm2
	movapd	xmm2, xmm0
	mulsd	xmm2, xmm4
	movapd	xmm5, xmm9
	mulsd	xmm5, xmm2
	movapd	xmm6, xmm9
	mulpd	xmm6, xmm4
	unpcklpd	xmm4, xmm5                      # xmm4 = xmm4[0],xmm5[0]
	addpd	xmm4, xmm9
	movsd	xmm4, xmm6                      # xmm4 = xmm6[0],xmm4[1]
	mulsd	xmm0, xmm2
	unpcklpd	xmm2, xmm0                      # xmm2 = xmm2[0],xmm0[0]
	unpcklpd	xmm3, xmm3                      # xmm3 = xmm3[0,0]
	mulpd	xmm2, xmm3
	addpd	xmm2, xmm4
	movapd	xmm0, xmm2
	addpd	xmm0, xmm2
	subpd	xmm0, xmm9
	mulsd	xmm1, xmm2
	subsd	xmm11, xmm1
	movapd	xmm1, xmm2
	unpckhpd	xmm1, xmm2                      # xmm1 = xmm1[1],xmm2[1]
	subsd	xmm11, xmm1
	mulsd	xmm1, xmm14
	mulsd	xmm11, xmm13
	addsd	xmm11, xmm1
	mulsd	xmm2, xmm7
	addsd	xmm2, xmm11
	movq	rax, xmm2
	movapd	xmm1, xmm2
	movapd	xmm3, xmmword ptr [rip + .LCPI4_38] # xmm3 = [NaN,NaN]
	andpd	xmm1, xmm3
	and	rax, r13
	movabs	rcx, 9218868437227405311
	cmp	rax, rcx
	setg	al
	ucomisd	xmm1, qword ptr [rip + .LCPI4_39]
	seta	cl
	movapd	xmm9, xmm0
	andpd	xmm9, xmm3
	movsd	xmm3, qword ptr [rip + .LCPI4_40] # xmm3 = [+Inf,0.0E+0]
	ucomisd	xmm9, xmm3
	sete	dl
	or	dl, al
	pshufd	xmm1, xmm9, 238                 # xmm1 = xmm9[2,3,2,3]
	ucomisd	xmm1, xmm3
	sete	al
	or	al, dl
	or	al, cl
	cmpltpd	xmm9, xmmword ptr [rip + .LCPI4_41]
	xorpd	xmm1, xmm1
	jne	.LBB4_59
# %bb.58:                               #   in Loop: Header=BB4_2 Depth=1
	xorps	xmm1, xmm1
	cvtsd2ss	xmm1, xmm2
	jmp	.LBB4_59
	.p2align	4
.LBB4_50:                               #   in Loop: Header=BB4_2 Depth=1
	movapd	xmm1, xmm2
	mov	eax, dword ptr [rsi + 304]
	movsd	xmm4, qword ptr [rip + .LCPI4_20] # xmm4 = [1.0E+0,0.0E+0]
	test	eax, eax
	je	.LBB4_53
.LBB4_52:                               #   in Loop: Header=BB4_2 Depth=1
	xorpd	xmm4, xmm4
	movsd	xmm5, qword ptr [rip + .LCPI4_20] # xmm5 = [1.0E+0,0.0E+0]
	cmp	eax, 1
	jne	.LBB4_54
	jmp	.LBB4_55
.LBB4_8:
	movsd	qword ptr [rsi + 248], xmm15
	movsd	qword ptr [rsi + 280], xmm14
	movsd	qword ptr [rsi + 288], xmm13
	movsd	qword ptr [rsi + 296], xmm7
	movsd	qword ptr [rsi + 72], xmm10
	movsd	qword ptr [rsi + 56], xmm6
	movupd	xmmword ptr [rsi + 264], xmm9
.LBB4_9:
	movaps	xmm6, xmmword ptr [rsp + 160]   # 16-byte Reload
	movaps	xmm7, xmmword ptr [rsp + 176]   # 16-byte Reload
	movaps	xmm8, xmmword ptr [rsp + 192]   # 16-byte Reload
	movaps	xmm9, xmmword ptr [rsp + 208]   # 16-byte Reload
	movaps	xmm10, xmmword ptr [rsp + 224]  # 16-byte Reload
	movaps	xmm11, xmmword ptr [rsp + 240]  # 16-byte Reload
	movaps	xmm12, xmmword ptr [rsp + 256]  # 16-byte Reload
	movaps	xmm13, xmmword ptr [rsp + 272]  # 16-byte Reload
	movaps	xmm14, xmmword ptr [rsp + 288]  # 16-byte Reload
	movaps	xmm15, xmmword ptr [rsp + 304]  # 16-byte Reload
	add	rsp, 328
	pop	rbx
	pop	rbp
	pop	rdi
	pop	rsi
	pop	r12
	pop	r13
	pop	r14
	pop	r15
	ret
	.section	.rdata,"dr",associative,_Z12sample_chainILb1EEvR5Chaini
	.p2align	2, 0x0
.LJTI4_0:
	.long	.LBB4_28-.LJTI4_0
	.long	.LBB4_29-.LJTI4_0
	.long	.LBB4_30-.LJTI4_0
	.long	.LBB4_31-.LJTI4_0
	.long	.LBB4_33-.LJTI4_0
	.long	.LBB4_34-.LJTI4_0
	.long	.LBB4_35-.LJTI4_0
	.long	.LBB4_36-.LJTI4_0
	.section	.text$_Z12sample_chainILb1EEvR5Chaini,"xr",discard,_Z12sample_chainILb1EEvR5Chaini
	.seh_endproc
                                        # -- End function
	.def	sample_calls;
