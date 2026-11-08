_ZN9easyknack19renderOscGainFilterILb0EEEvRNS_14OscillatorNodeERNS_8MathNodeERNS_10FilterNodeEPfS7_S7_yd: # @_ZN9easyknack19renderOscGainFilterILb0EEEvRNS_14OscillatorNodeERNS_8MathNodeERNS_10FilterNodeEPfS7_S7_yd
.seh_proc _ZN9easyknack19renderOscGainFilterILb0EEEvRNS_14OscillatorNodeERNS_8MathNodeERNS_10FilterNodeEPfS7_S7_yd
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
	sub	rsp, 248
	.seh_stackalloc 248
	movapd	xmmword ptr [rsp + 224], xmm15  # 16-byte Spill
	.seh_savexmm xmm15, 224
	movapd	xmmword ptr [rsp + 208], xmm14  # 16-byte Spill
	.seh_savexmm xmm14, 208
	movapd	xmmword ptr [rsp + 192], xmm13  # 16-byte Spill
	.seh_savexmm xmm13, 192
	movapd	xmmword ptr [rsp + 176], xmm12  # 16-byte Spill
	.seh_savexmm xmm12, 176
	movapd	xmmword ptr [rsp + 160], xmm11  # 16-byte Spill
	.seh_savexmm xmm11, 160
	movaps	xmmword ptr [rsp + 144], xmm10  # 16-byte Spill
	.seh_savexmm xmm10, 144
	movaps	xmmword ptr [rsp + 128], xmm9   # 16-byte Spill
	.seh_savexmm xmm9, 128
	movaps	xmmword ptr [rsp + 112], xmm8   # 16-byte Spill
	.seh_savexmm xmm8, 112
	movapd	xmmword ptr [rsp + 96], xmm7    # 16-byte Spill
	.seh_savexmm xmm7, 96
	movapd	xmmword ptr [rsp + 80], xmm6    # 16-byte Spill
	.seh_savexmm xmm6, 80
	.seh_endprologue
	mov	rbx, r9
	mov	rsi, r8
	mov	rdi, rdx
	mov	r15, rcx
	movsd	xmm8, qword ptr [rsp + 376]     # xmm8 = mem[0],zero
	movsd	xmm1, qword ptr [rip + .LCPI426_0] # xmm1 = [5.0000000000000001E-3,0.0E+0]
	mulsd	xmm1, xmm8
	movsd	xmm0, qword ptr [rip + .LCPI426_1] # xmm0 = [-1.0E+0,0.0E+0]
	divsd	xmm0, xmm1
	call	exp
	cmp	qword ptr [rsp + 368], 0
	je	.LBB426_43
# %bb.1:
	movsd	xmm10, qword ptr [rip + .LCPI426_2] # xmm10 = [1.0E+0,0.0E+0]
	subsd	xmm10, xmm0
	xorps	xmm0, xmm0
	cvtsd2ss	xmm0, xmm10
	movaps	xmmword ptr [rsp + 32], xmm0    # 16-byte Spill
	movss	xmm0, dword ptr [r15 + 64]      # xmm0 = mem[0],zero,zero,zero
	xorps	xmm9, xmm9
	cvtss2sd	xmm9, xmm0
	mov	r12d, dword ptr [r15 + 80]
	imul	r14, r12, 81960
	add	r14, qword ptr [r15 + 88]
	movsd	xmm6, qword ptr [r15 + 56]      # xmm6 = mem[0],zero
	mov	qword ptr [rsp + 48], r15       # 8-byte Spill
	movsd	xmm11, qword ptr [r15 + 72]     # xmm11 = mem[0],zero
	xor	r13d, r13d
	movabs	rbp, 9223372036854775807
	movsd	xmm14, qword ptr [rip + .LCPI426_4] # xmm14 = [5.0E-1,0.0E+0]
	movsd	xmm15, qword ptr [rip + .LCPI426_13] # xmm15 = [6.2831853071795862E+0,0.0E+0]
	movapd	xmm12, xmmword ptr [rip + .LCPI426_17] # xmm12 = [+Inf,+Inf]
	movapd	xmm13, xmmword ptr [rip + .LCPI426_18] # xmm13 = [-1.0E+0,-1.0E+0]
	mov	r15, rbp
	jmp	.LBB426_2
	.p2align	4
.LBB426_5:                              #   in Loop: Header=BB426_2 Depth=1
	xor	eax, eax
.LBB426_21:                             #   in Loop: Header=BB426_2 Depth=1
	mov	ecx, eax
	shl	ecx, 13
	lea	ebp, [rcx + 4*rax]
	add	rbp, r14
	divsd	xmm6, xmm15
	movapd	xmm0, xmm6
	call	floor
	movapd	xmm1, xmm6
	subsd	xmm1, xmm0
	mulsd	xmm1, qword ptr [rip + .LCPI426_14]
	cvttsd2si	rcx, xmm1
	mov	rdx, rcx
	sar	rdx, 63
	movapd	xmm0, xmm1
	subsd	xmm0, qword ptr [rip + .LCPI426_15]
	cvttsd2si	rax, xmm0
	and	rax, rdx
	or	rax, rcx
	lea	ecx, [rax + 1]
	and	ecx, 2047
	movss	xmm0, dword ptr [rbp + 4*rax]   # xmm0 = mem[0],zero,zero,zero
	movss	xmm2, dword ptr [rbp + 4*rcx]   # xmm2 = mem[0],zero,zero,zero
	unpcklps	xmm2, xmm0                      # xmm2 = xmm2[0],xmm0[0],xmm2[1],xmm0[1]
	cvtps2pd	xmm2, xmm2
	movapd	xmm0, xmm2
	andpd	xmm0, xmmword ptr [rip + .LCPI426_16]
	movapd	xmm3, xmm0
	cmpneqpd	xmm3, xmm12
	cmpordpd	xmm0, xmm12
	andpd	xmm0, xmm3
	andpd	xmm0, xmm2
	movapd	xmm2, xmmword ptr [rip + .LCPI426_19] # xmm2 = [1.0E+0,1.0E+0]
	minpd	xmm2, xmm0
	cmpltpd	xmm0, xmm13
	movapd	xmm3, xmm13
	andpd	xmm3, xmm0
	andnpd	xmm0, xmm2
	orpd	xmm0, xmm3
	pshufd	xmm2, xmm0, 238                 # xmm2 = xmm0[2,3,2,3]
	subsd	xmm0, xmm2
	movq	xmm3, rax
	punpckldq	xmm3, xmmword ptr [rip + .LCPI426_20] # xmm3 = xmm3[0],mem[0],xmm3[1],mem[1]
	subpd	xmm3, xmmword ptr [rip + .LCPI426_21]
	movapd	xmm4, xmm3
	unpckhpd	xmm4, xmm3                      # xmm4 = xmm4[1],xmm3[1]
	addsd	xmm4, xmm3
	subsd	xmm1, xmm4
	mulsd	xmm1, xmm0
	addsd	xmm1, xmm2
	xorps	xmm0, xmm0
	cvtsd2ss	xmm0, xmm1
	movss	dword ptr [rbx + 4*r13], xmm0
	addsd	xmm7, xmm6
	movapd	xmm0, xmm7
	call	floor
	subsd	xmm7, xmm0
	movapd	xmm6, xmm7
	mulsd	xmm6, xmm15
	inc	r13
	cmp	qword ptr [rsp + 368], r13
	mov	rbp, r15
	je	.LBB426_6
.LBB426_2:                              # =>This Inner Loop Header: Depth=1
	movapd	xmm0, xmm11
	movaps	xmm11, xmm9
	subsd	xmm11, xmm0
	mulsd	xmm11, xmm10
	addsd	xmm11, xmm0
	movq	rax, xmm11
	and	rax, rbp
	xorpd	xmm0, xmm0
	movapd	xmm7, xmm11
	movabs	rcx, 9218868437227405312
	cmp	rax, rcx
	jl	.LBB426_4
# %bb.3:                                #   in Loop: Header=BB426_2 Depth=1
	xorpd	xmm7, xmm7
.LBB426_4:                              #   in Loop: Header=BB426_2 Depth=1
	movsd	xmm1, qword ptr [rip + .LCPI426_3] # xmm1 = [2.0E+4,0.0E+0]
	minsd	xmm1, xmm7
	cmpltsd	xmm7, xmm0
	andnpd	xmm7, xmm1
	divsd	xmm7, xmm8
	test	r12d, r12d
	je	.LBB426_5
# %bb.11:                               #   in Loop: Header=BB426_2 Depth=1
	movapd	xmm0, xmm7
	addsd	xmm0, xmm7
	ucomisd	xmm14, xmm0
	mov	eax, 0
	jbe	.LBB426_21
# %bb.12:                               #   in Loop: Header=BB426_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI426_5]
	mov	eax, 1
	ucomisd	xmm14, xmm0
	jbe	.LBB426_21
# %bb.13:                               #   in Loop: Header=BB426_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI426_6]
	mov	eax, 2
	ucomisd	xmm14, xmm0
	jbe	.LBB426_21
# %bb.14:                               #   in Loop: Header=BB426_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI426_7]
	mov	eax, 3
	ucomisd	xmm14, xmm0
	jbe	.LBB426_21
# %bb.15:                               #   in Loop: Header=BB426_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI426_8]
	mov	eax, 4
	ucomisd	xmm14, xmm0
	jbe	.LBB426_21
# %bb.16:                               #   in Loop: Header=BB426_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI426_9]
	mov	eax, 5
	ucomisd	xmm14, xmm0
	jbe	.LBB426_21
# %bb.17:                               #   in Loop: Header=BB426_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI426_10]
	mov	eax, 6
	ucomisd	xmm14, xmm0
	jbe	.LBB426_21
# %bb.18:                               #   in Loop: Header=BB426_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI426_11]
	mov	eax, 7
	ucomisd	xmm14, xmm0
	jbe	.LBB426_21
# %bb.19:                               #   in Loop: Header=BB426_2 Depth=1
	movapd	xmm0, xmm7
	mulsd	xmm0, qword ptr [rip + .LCPI426_12]
	mov	eax, 8
	ucomisd	xmm14, xmm0
	jbe	.LBB426_21
# %bb.20:                               #   in Loop: Header=BB426_2 Depth=1
	mov	eax, 9
	jmp	.LBB426_21
.LBB426_6:
	mov	rax, qword ptr [rsp + 48]       # 8-byte Reload
	movsd	qword ptr [rax + 72], xmm11
	movsd	qword ptr [rax + 56], xmm6
	mov	r15, qword ptr [rsp + 368]
	cmp	r15, 1
	jne	.LBB426_7
# %bb.22:
	movss	xmm0, dword ptr [rbx]           # xmm0 = mem[0],zero,zero,zero
	movsd	xmm1, qword ptr [rdi + 56]      # xmm1 = mem[0],zero
	movsd	qword ptr [rdi + 64], xmm1
	mulss	xmm0, dword ptr [rdi + 60]
	movd	eax, xmm0
	and	eax, 2147483647
	cmp	eax, 2139095040
	mov	r14, qword ptr [rsp + 360]
	mov	r12, qword ptr [rsp + 352]
	jl	.LBB426_24
# %bb.23:
	xorps	xmm0, xmm0
.LBB426_24:
	movss	dword ptr [r12], xmm0
.LBB426_25:
	movsd	xmm4, qword ptr [rip + .LCPI426_22] # xmm4 = [4.5000000000000001E-1,0.0E+0]
	mulsd	xmm4, xmm8
	movups	xmm1, xmmword ptr [rsi + 56]
	movupd	xmm0, xmmword ptr [rsi + 72]
	movupd	xmm15, xmmword ptr [rsi + 88]
	movaps	xmm5, xmm1
	shufps	xmm5, xmm1, 245                 # xmm5 = xmm5[1,1],xmm1[3,3]
	andps	xmm5, xmmword ptr [rip + .LCPI426_23]
	pcmpgtd	xmm5, xmmword ptr [rip + .LCPI426_24]
	movapd	xmm2, xmmword ptr [rip + .LCPI426_25] # xmm2 = [5.0E+0,1.0000000000000001E-1]
	movapd	xmm3, xmmword ptr [rip + .LCPI426_26] # xmm3 = [u,3.0E+1]
	movapd	xmmword ptr [rsp + 48], xmm4    # 16-byte Spill
	movsd	xmm3, xmm4                      # xmm3 = xmm4[0],xmm3[1]
	minpd	xmm3, xmm1
	cmpltpd	xmm1, xmm2
	andpd	xmm2, xmm1
	andnpd	xmm1, xmm3
	orpd	xmm1, xmm2
	movdqa	xmm2, xmm5
	pandn	xmm2, xmm1
	pand	xmm5, xmmword ptr [rip + .LCPI426_27]
	por	xmm5, xmm2
	movdqa	xmmword ptr [rsp + 32], xmm5    # 16-byte Spill
	movsd	xmm14, qword ptr [rsi + 104]    # xmm14 = mem[0],zero
	movsd	xmm13, qword ptr [rsi + 112]    # xmm13 = mem[0],zero
	movsd	xmm12, qword ptr [rsi + 120]    # xmm12 = mem[0],zero
	movapd	xmm1, xmm10
	unpcklpd	xmm1, xmm10                     # xmm1 = xmm1[0],xmm10[0]
	movapd	xmmword ptr [rsp + 64], xmm1    # 16-byte Spill
	xor	edi, edi
	movabs	rbx, 9218868437227405311
	jmp	.LBB426_26
	.p2align	4
.LBB426_41:                             #   in Loop: Header=BB426_26 Depth=1
	movzx	eax, al
	movd	xmm3, eax
	pshufd	xmm3, xmm3, 0                   # xmm3 = xmm3[0,0,0,0]
	shufps	xmm15, xmm15, 160               # xmm15 = xmm15[0,0,2,2]
	orps	xmm15, xmm3
	pslld	xmm15, 31
	psrad	xmm15, 31
	pandn	xmm15, xmm0
	movss	dword ptr [r14 + 4*rdi], xmm2
	inc	rdi
	movapd	xmm0, xmm7
	unpcklpd	xmm0, xmm1                      # xmm0 = xmm0[0],xmm1[0]
	cmp	r15, rdi
	je	.LBB426_42
.LBB426_26:                             # =>This Inner Loop Header: Depth=1
	movapd	xmm7, xmmword ptr [rsp + 32]    # 16-byte Reload
	subpd	xmm7, xmm0
	mulpd	xmm7, xmmword ptr [rsp + 64]    # 16-byte Folded Reload
	addpd	xmm7, xmm0
	movapd	xmm8, xmm7
	unpckhpd	xmm8, xmm7                      # xmm8 = xmm8[1],xmm7[1]
	movhpd	qword ptr [rsi + 80], xmm7
	movapd	xmm1, xmmword ptr [rsp + 48]    # 16-byte Reload
	minsd	xmm1, xmm7
	mulsd	xmm1, qword ptr [rip + .LCPI426_28]
	movapd	xmm0, xmm7
	cmpltsd	xmm0, qword ptr [rip + .LCPI426_29]
	movapd	xmm2, xmm0
	andnpd	xmm2, xmm1
	movsd	xmm1, qword ptr [rip + .LCPI426_30] # xmm1 = [1.5707963267948966E+1,0.0E+0]
	andpd	xmm0, xmm1
	orpd	xmm0, xmm2
	divsd	xmm0, qword ptr [rsp + 376]
	call	tan
	ucomisd	xmm8, qword ptr [rip + .LCPI426_31]
	movsd	xmm1, qword ptr [rsi + 80]      # xmm1 = mem[0],zero
	movsd	xmm3, qword ptr [rip + .LCPI426_32] # xmm3 = [3.3333333333333333E-2,0.0E+0]
	ja	.LBB426_28
# %bb.27:                               #   in Loop: Header=BB426_26 Depth=1
	movsd	xmm3, qword ptr [rip + .LCPI426_2] # xmm3 = [1.0E+0,0.0E+0]
	divsd	xmm3, xmm1
.LBB426_28:                             #   in Loop: Header=BB426_26 Depth=1
	movapd	xmm2, xmm7
	cmpltpd	xmm2, xmmword ptr [rip + .LCPI426_25]
	pextrw	eax, xmm2, 4
	movsd	xmm2, qword ptr [rip + .LCPI426_33] # xmm2 = [1.0E+1,0.0E+0]
	test	al, 1
	jne	.LBB426_30
# %bb.29:                               #   in Loop: Header=BB426_26 Depth=1
	movapd	xmm2, xmm3
.LBB426_30:                             #   in Loop: Header=BB426_26 Depth=1
	movd	xmm3, dword ptr [r12 + 4*rdi]   # xmm3 = mem[0],zero,zero,zero
	movd	eax, xmm3
	and	eax, 2147483647
	cmp	eax, 2139095040
	jl	.LBB426_31
# %bb.32:                               #   in Loop: Header=BB426_26 Depth=1
	xorps	xmm3, xmm3
	mov	eax, dword ptr [rsi + 128]
	movsd	xmm9, qword ptr [rip + .LCPI426_2] # xmm9 = [1.0E+0,0.0E+0]
	test	eax, eax
	je	.LBB426_35
.LBB426_34:                             #   in Loop: Header=BB426_26 Depth=1
	xorpd	xmm9, xmm9
.LBB426_35:                             #   in Loop: Header=BB426_26 Depth=1
	movsd	xmm8, qword ptr [rip + .LCPI426_2] # xmm8 = [1.0E+0,0.0E+0]
	cmp	eax, 1
	je	.LBB426_37
# %bb.36:                               #   in Loop: Header=BB426_26 Depth=1
	xorpd	xmm8, xmm8
.LBB426_37:                             #   in Loop: Header=BB426_26 Depth=1
	movapd	xmm4, xmm0
	addsd	xmm4, xmm2
	mulsd	xmm4, xmm0
	movsd	xmm6, qword ptr [rip + .LCPI426_2] # xmm6 = [1.0E+0,0.0E+0]
	addsd	xmm4, xmm6
	movapd	xmm5, xmm6
	divsd	xmm5, xmm4
	movapd	xmm11, xmm15
	unpckhpd	xmm11, xmm15                    # xmm11 = xmm11[1],xmm15[1]
	movaps	xmm4, xmm3
	subsd	xmm4, xmm11
	subsd	xmm9, xmm14
	mulsd	xmm9, xmm10
	addsd	xmm14, xmm9
	subsd	xmm8, xmm13
	mulsd	xmm8, xmm10
	addsd	xmm13, xmm8
	cmp	eax, 2
	je	.LBB426_39
# %bb.38:                               #   in Loop: Header=BB426_26 Depth=1
	xorpd	xmm6, xmm6
.LBB426_39:                             #   in Loop: Header=BB426_26 Depth=1
	subsd	xmm6, xmm12
	mulsd	xmm6, xmm10
	addsd	xmm12, xmm6
	movapd	xmm8, xmm0
	mulsd	xmm8, xmm5
	movapd	xmm9, xmm8
	mulsd	xmm9, xmm15
	movapd	xmm11, xmm5
	unpcklpd	xmm11, xmm9                     # xmm11 = xmm11[0],xmm9[0]
	mulpd	xmm5, xmm15
	addpd	xmm11, xmm15
	movsd	xmm11, xmm5                     # xmm11 = xmm5[0],xmm11[1]
	mulsd	xmm0, xmm8
	unpcklpd	xmm8, xmm0                      # xmm8 = xmm8[0],xmm0[0]
	unpcklpd	xmm4, xmm4                      # xmm4 = xmm4[0,0]
	mulpd	xmm4, xmm8
	addpd	xmm4, xmm11
	movapd	xmm0, xmm4
	addpd	xmm0, xmm4
	subpd	xmm0, xmm15
	mulsd	xmm2, xmm4
	subsd	xmm3, xmm2
	movapd	xmm2, xmm4
	unpckhpd	xmm2, xmm4                      # xmm2 = xmm2[1],xmm4[1]
	subsd	xmm3, xmm2
	mulsd	xmm2, xmm14
	mulsd	xmm3, xmm13
	addsd	xmm3, xmm2
	mulsd	xmm4, xmm12
	addsd	xmm4, xmm3
	movq	rax, xmm4
	movapd	xmm2, xmm4
	movapd	xmm3, xmmword ptr [rip + .LCPI426_16] # xmm3 = [NaN,NaN]
	andpd	xmm2, xmm3
	and	rax, rbp
	cmp	rax, rbx
	setg	al
	ucomisd	xmm2, qword ptr [rip + .LCPI426_34]
	seta	cl
	movapd	xmm15, xmm0
	andpd	xmm15, xmm3
	movsd	xmm3, qword ptr [rip + .LCPI426_35] # xmm3 = [+Inf,0.0E+0]
	ucomisd	xmm15, xmm3
	sete	dl
	or	dl, al
	pshufd	xmm2, xmm15, 238                # xmm2 = xmm15[2,3,2,3]
	ucomisd	xmm2, xmm3
	sete	al
	or	al, dl
	or	al, cl
	cmpltpd	xmm15, xmmword ptr [rip + .LCPI426_36]
	xorpd	xmm2, xmm2
	jne	.LBB426_41
# %bb.40:                               #   in Loop: Header=BB426_26 Depth=1
	xorps	xmm2, xmm2
	cvtsd2ss	xmm2, xmm4
	jmp	.LBB426_41
	.p2align	4
.LBB426_31:                             #   in Loop: Header=BB426_26 Depth=1
	cvtss2sd	xmm3, xmm3
	mov	eax, dword ptr [rsi + 128]
	movsd	xmm9, qword ptr [rip + .LCPI426_2] # xmm9 = [1.0E+0,0.0E+0]
	test	eax, eax
	jne	.LBB426_34
	jmp	.LBB426_35
.LBB426_42:
	movsd	qword ptr [rsi + 72], xmm7
	movsd	qword ptr [rsi + 104], xmm14
	movsd	qword ptr [rsi + 112], xmm13
	movsd	qword ptr [rsi + 120], xmm12
	movupd	xmmword ptr [rsi + 88], xmm15
.LBB426_43:
	movaps	xmm6, xmmword ptr [rsp + 80]    # 16-byte Reload
	movaps	xmm7, xmmword ptr [rsp + 96]    # 16-byte Reload
	movaps	xmm8, xmmword ptr [rsp + 112]   # 16-byte Reload
	movaps	xmm9, xmmword ptr [rsp + 128]   # 16-byte Reload
	movaps	xmm10, xmmword ptr [rsp + 144]  # 16-byte Reload
	movaps	xmm11, xmmword ptr [rsp + 160]  # 16-byte Reload
	movaps	xmm12, xmmword ptr [rsp + 176]  # 16-byte Reload
	movaps	xmm13, xmmword ptr [rsp + 192]  # 16-byte Reload
	movaps	xmm14, xmmword ptr [rsp + 208]  # 16-byte Reload
	movaps	xmm15, xmmword ptr [rsp + 224]  # 16-byte Reload
	add	rsp, 248
	pop	rbx
	pop	rbp
	pop	rdi
	pop	rsi
	pop	r12
	pop	r13
	pop	r14
	pop	r15
	ret
.LBB426_7:
	movaps	xmm2, xmmword ptr [rsp + 32]    # 16-byte Reload
	shufps	xmm2, xmm2, 0                   # xmm2 = xmm2[0,0,0,0]
	xor	eax, eax
	mov	r14, qword ptr [rsp + 360]
	mov	r12, qword ptr [rsp + 352]
	jmp	.LBB426_8
	.p2align	4
.LBB426_10:                             #   in Loop: Header=BB426_8 Depth=1
	movss	dword ptr [r12 + 4*rax], xmm0
	inc	rax
	cmp	r15, rax
	je	.LBB426_25
.LBB426_8:                              # =>This Inner Loop Header: Depth=1
	movsd	xmm1, qword ptr [rdi + 56]      # xmm1 = mem[0],zero
	movsd	xmm0, qword ptr [rdi + 64]      # xmm0 = mem[0],zero
	subps	xmm1, xmm0
	mulps	xmm1, xmm2
	addps	xmm1, xmm0
	movaps	xmm0, xmm1
	shufps	xmm0, xmm1, 85                  # xmm0 = xmm0[1,1],xmm1[1,1]
	mulss	xmm0, dword ptr [rbx + 4*rax]
	movlps	qword ptr [rdi + 64], xmm1
	movd	ecx, xmm0
	and	ecx, 2147483647
	cmp	ecx, 2139095040
	jl	.LBB426_10
# %bb.9:                                #   in Loop: Header=BB426_8 Depth=1
	xorps	xmm0, xmm0
	jmp	.LBB426_10
	.seh_endproc
                                        # -- End function

_ZN9easyknack16HandwrittenChain7processERNS_14ProcessContextE: # @_ZN9easyknack16HandwrittenChain7processERNS_14ProcessContextE
.seh_proc _ZN9easyknack16HandwrittenChain7processERNS_14ProcessContextE
# %bb.0:
	push	r15
	.seh_pushreg r15
	push	r14
	.seh_pushreg r14
	push	r12
	.seh_pushreg r12
	push	rsi
	.seh_pushreg rsi
	push	rdi
	.seh_pushreg rdi
	push	rbx
	.seh_pushreg rbx
	sub	rsp, 296
	.seh_stackalloc 296
	movaps	xmmword ptr [rsp + 272], xmm15  # 16-byte Spill
	.seh_savexmm xmm15, 272
	movaps	xmmword ptr [rsp + 256], xmm14  # 16-byte Spill
	.seh_savexmm xmm14, 256
	movaps	xmmword ptr [rsp + 240], xmm13  # 16-byte Spill
	.seh_savexmm xmm13, 240
	movaps	xmmword ptr [rsp + 224], xmm12  # 16-byte Spill
	.seh_savexmm xmm12, 224
	movaps	xmmword ptr [rsp + 208], xmm11  # 16-byte Spill
	.seh_savexmm xmm11, 208
	movapd	xmmword ptr [rsp + 192], xmm10  # 16-byte Spill
	.seh_savexmm xmm10, 192
	movaps	xmmword ptr [rsp + 176], xmm9   # 16-byte Spill
	.seh_savexmm xmm9, 176
	movaps	xmmword ptr [rsp + 160], xmm8   # 16-byte Spill
	.seh_savexmm xmm8, 160
	movaps	xmmword ptr [rsp + 144], xmm7   # 16-byte Spill
	.seh_savexmm xmm7, 144
	movaps	xmmword ptr [rsp + 128], xmm6   # 16-byte Spill
	.seh_savexmm xmm6, 128
	.seh_endprologue
	mov	rdi, rdx
	mov	rsi, rcx
	lea	rbx, [rcx + 56]
	lea	r14, [rcx + 392]
	movss	xmm0, dword ptr [rcx + 120]     # xmm0 = mem[0],zero,zero,zero
	xorps	xmm7, xmm7
	cvtss2sd	xmm7, xmm0
	mov	r15, qword ptr [rdx + 24]
	movsd	xmm8, qword ptr [rdx + 32]      # xmm8 = mem[0],zero
	movsd	xmm9, qword ptr [rip + .LCPI436_0] # xmm9 = [5.0000000000000001E-3,0.0E+0]
	movapd	xmm1, xmm8
	mulsd	xmm1, xmm9
	movsd	xmm6, qword ptr [rip + .LCPI436_1] # xmm6 = [-1.0E+0,0.0E+0]
	movapd	xmm0, xmm6
	divsd	xmm0, xmm1
	call	exp
	movsd	xmm15, qword ptr [rip + .LCPI436_2] # xmm15 = [1.0E+0,0.0E+0]
	movapd	xmm1, xmm15
	subsd	xmm1, xmm0
	movsd	qword ptr [rsp + 64], xmm1
	xorpd	xmm0, xmm0
	movupd	xmmword ptr [rsp + 40], xmm0
	movsd	qword ptr [rsp + 32], xmm8
	mov	byte ptr [rsp + 56], 1
	mov	rcx, rbx
	mov	rdx, r14
	mov	r8, r15
	movaps	xmm3, xmm7
	call	_ZN9easyknack14OscillatorNode14renderPreparedEPfyddPKfS3_bd
	movsd	xmm0, qword ptr [rdi + 32]      # xmm0 = mem[0],zero
	movsd	qword ptr [rsp + 72], xmm0      # 8-byte Spill
	mulsd	xmm9, xmm0
	divsd	xmm6, xmm9
	movapd	xmm0, xmm6
	call	exp
	mov	rbx, qword ptr [rdi + 24]
	test	rbx, rbx
	je	.LBB436_27
# %bb.1:
	movsd	xmm1, qword ptr [rsi + 216]     # xmm1 = mem[0],zero
	cmp	rbx, 1
	jne	.LBB436_2
# %bb.6:
	movss	xmm0, dword ptr [rsi + 392]     # xmm0 = mem[0],zero,zero,zero
	mulss	xmm0, dword ptr [rsi + 220]
	movd	eax, xmm0
	and	eax, 2147483647
	cmp	eax, 2139095040
	jl	.LBB436_8
# %bb.7:
	xorps	xmm0, xmm0
.LBB436_8:
	movss	dword ptr [rsi + 1416], xmm0
	movaps	xmm0, xmm1
.LBB436_9:
	movlps	qword ptr [rsi + 224], xmm0
	mov	rdi, qword ptr [rdi + 16]
	movapd	xmm0, xmm6
	call	exp
	movapd	xmm6, xmm15
	subsd	xmm6, xmm0
	movsd	xmm4, qword ptr [rip + .LCPI436_3] # xmm4 = [4.5000000000000001E-1,0.0E+0]
	mulsd	xmm4, qword ptr [rsp + 72]      # 8-byte Folded Reload
	movups	xmm1, xmmword ptr [rsi + 304]
	movupd	xmm0, xmmword ptr [rsi + 320]
	movupd	xmm14, xmmword ptr [rsi + 336]
	movaps	xmm5, xmm1
	shufps	xmm5, xmm1, 245                 # xmm5 = xmm5[1,1],xmm1[3,3]
	andps	xmm5, xmmword ptr [rip + .LCPI436_4]
	pcmpgtd	xmm5, xmmword ptr [rip + .LCPI436_5]
	movapd	xmm2, xmmword ptr [rip + .LCPI436_6] # xmm2 = [5.0E+0,1.0000000000000001E-1]
	movapd	xmm3, xmmword ptr [rip + .LCPI436_7] # xmm3 = [u,3.0E+1]
	movapd	xmmword ptr [rsp + 112], xmm4   # 16-byte Spill
	movsd	xmm3, xmm4                      # xmm3 = xmm4[0],xmm3[1]
	minpd	xmm3, xmm1
	cmpltpd	xmm1, xmm2
	andpd	xmm2, xmm1
	andnpd	xmm1, xmm3
	orpd	xmm1, xmm2
	movsd	xmm13, qword ptr [rsi + 352]    # xmm13 = mem[0],zero
	movsd	xmm12, qword ptr [rsi + 360]    # xmm12 = mem[0],zero
	movsd	xmm11, qword ptr [rsi + 368]    # xmm11 = mem[0],zero
	movdqa	xmm2, xmm5
	pandn	xmm2, xmm1
	pand	xmm5, xmmword ptr [rip + .LCPI436_8]
	por	xmm5, xmm2
	movdqa	xmmword ptr [rsp + 96], xmm5    # 16-byte Spill
	movapd	xmm1, xmm6
	unpcklpd	xmm1, xmm6                      # xmm1 = xmm1[0],xmm6[0]
	movapd	xmmword ptr [rsp + 80], xmm1    # 16-byte Spill
	xor	r14d, r14d
	movabs	r15, 9223372036854775807
	movabs	r12, 9218868437227405311
	jmp	.LBB436_10
	.p2align	4
.LBB436_25:                             #   in Loop: Header=BB436_10 Depth=1
	movzx	eax, al
	movd	xmm3, eax
	pshufd	xmm3, xmm3, 0                   # xmm3 = xmm3[0,0,0,0]
	shufps	xmm14, xmm14, 160               # xmm14 = xmm14[0,0,2,2]
	orps	xmm14, xmm3
	pslld	xmm14, 31
	psrad	xmm14, 31
	pandn	xmm14, xmm0
	movss	dword ptr [rdi + 4*r14], xmm2
	inc	r14
	movapd	xmm0, xmm10
	unpcklpd	xmm0, xmm1                      # xmm0 = xmm0[0],xmm1[0]
	cmp	rbx, r14
	je	.LBB436_26
.LBB436_10:                             # =>This Inner Loop Header: Depth=1
	movapd	xmm10, xmmword ptr [rsp + 96]   # 16-byte Reload
	subpd	xmm10, xmm0
	mulpd	xmm10, xmmword ptr [rsp + 80]   # 16-byte Folded Reload
	addpd	xmm10, xmm0
	movapd	xmm8, xmm10
	unpckhpd	xmm8, xmm10                     # xmm8 = xmm8[1],xmm10[1]
	movhpd	qword ptr [rsi + 328], xmm10
	movapd	xmm1, xmmword ptr [rsp + 112]   # 16-byte Reload
	minsd	xmm1, xmm10
	mulsd	xmm1, qword ptr [rip + .LCPI436_9]
	movapd	xmm0, xmm10
	cmpltsd	xmm0, qword ptr [rip + .LCPI436_10]
	movapd	xmm2, xmm0
	andnpd	xmm2, xmm1
	movsd	xmm1, qword ptr [rip + .LCPI436_11] # xmm1 = [1.5707963267948966E+1,0.0E+0]
	andpd	xmm0, xmm1
	orpd	xmm0, xmm2
	divsd	xmm0, qword ptr [rsp + 72]      # 8-byte Folded Reload
	call	tan
	ucomisd	xmm8, qword ptr [rip + .LCPI436_12]
	movsd	xmm1, qword ptr [rsi + 328]     # xmm1 = mem[0],zero
	movsd	xmm3, qword ptr [rip + .LCPI436_13] # xmm3 = [3.3333333333333333E-2,0.0E+0]
	ja	.LBB436_12
# %bb.11:                               #   in Loop: Header=BB436_10 Depth=1
	movapd	xmm3, xmm15
	divsd	xmm3, xmm1
.LBB436_12:                             #   in Loop: Header=BB436_10 Depth=1
	movapd	xmm2, xmm10
	cmpltpd	xmm2, xmmword ptr [rip + .LCPI436_6]
	pextrw	eax, xmm2, 4
	movsd	xmm2, qword ptr [rip + .LCPI436_14] # xmm2 = [1.0E+1,0.0E+0]
	test	al, 1
	jne	.LBB436_14
# %bb.13:                               #   in Loop: Header=BB436_10 Depth=1
	movapd	xmm2, xmm3
.LBB436_14:                             #   in Loop: Header=BB436_10 Depth=1
	movd	xmm3, dword ptr [rsi + 4*r14 + 1416] # xmm3 = mem[0],zero,zero,zero
	movd	eax, xmm3
	and	eax, 2147483647
	cmp	eax, 2139095040
	jl	.LBB436_15
# %bb.16:                               #   in Loop: Header=BB436_10 Depth=1
	xorps	xmm3, xmm3
	mov	eax, dword ptr [rsi + 376]
	movapd	xmm8, xmm15
	test	eax, eax
	je	.LBB436_19
.LBB436_18:                             #   in Loop: Header=BB436_10 Depth=1
	xorpd	xmm8, xmm8
.LBB436_19:                             #   in Loop: Header=BB436_10 Depth=1
	movapd	xmm9, xmm15
	cmp	eax, 1
	je	.LBB436_21
# %bb.20:                               #   in Loop: Header=BB436_10 Depth=1
	xorpd	xmm9, xmm9
.LBB436_21:                             #   in Loop: Header=BB436_10 Depth=1
	movapd	xmm4, xmm0
	addsd	xmm4, xmm2
	mulsd	xmm4, xmm0
	addsd	xmm4, xmm15
	movapd	xmm7, xmm14
	unpckhpd	xmm7, xmm14                     # xmm7 = xmm7[1],xmm14[1]
	movaps	xmm5, xmm3
	subsd	xmm5, xmm7
	subsd	xmm8, xmm13
	mulsd	xmm8, xmm6
	addsd	xmm13, xmm8
	subsd	xmm9, xmm12
	mulsd	xmm9, xmm6
	addsd	xmm12, xmm9
	movapd	xmm8, xmm15
	cmp	eax, 2
	je	.LBB436_23
# %bb.22:                               #   in Loop: Header=BB436_10 Depth=1
	xorpd	xmm8, xmm8
.LBB436_23:                             #   in Loop: Header=BB436_10 Depth=1
	subsd	xmm8, xmm11
	mulsd	xmm8, xmm6
	addsd	xmm11, xmm8
	movapd	xmm7, xmm15
	divsd	xmm7, xmm4
	movapd	xmm4, xmm0
	mulsd	xmm4, xmm7
	movapd	xmm8, xmm14
	mulsd	xmm8, xmm4
	movapd	xmm9, xmm14
	mulpd	xmm9, xmm7
	unpcklpd	xmm7, xmm8                      # xmm7 = xmm7[0],xmm8[0]
	addpd	xmm7, xmm14
	movsd	xmm7, xmm9                      # xmm7 = xmm9[0],xmm7[1]
	mulsd	xmm0, xmm4
	unpcklpd	xmm4, xmm0                      # xmm4 = xmm4[0],xmm0[0]
	unpcklpd	xmm5, xmm5                      # xmm5 = xmm5[0,0]
	mulpd	xmm4, xmm5
	addpd	xmm4, xmm7
	movapd	xmm0, xmm4
	addpd	xmm0, xmm4
	subpd	xmm0, xmm14
	mulsd	xmm2, xmm4
	subsd	xmm3, xmm2
	movapd	xmm2, xmm4
	unpckhpd	xmm2, xmm4                      # xmm2 = xmm2[1],xmm4[1]
	subsd	xmm3, xmm2
	mulsd	xmm2, xmm13
	mulsd	xmm3, xmm12
	addsd	xmm3, xmm2
	mulsd	xmm4, xmm11
	addsd	xmm4, xmm3
	movq	rax, xmm4
	and	rax, r15
	movapd	xmm2, xmm4
	movapd	xmm3, xmmword ptr [rip + .LCPI436_15] # xmm3 = [NaN,NaN]
	andpd	xmm2, xmm3
	cmp	rax, r12
	setg	al
	ucomisd	xmm2, qword ptr [rip + .LCPI436_16]
	seta	cl
	movapd	xmm14, xmm0
	andpd	xmm14, xmm3
	movsd	xmm3, qword ptr [rip + .LCPI436_17] # xmm3 = [+Inf,0.0E+0]
	ucomisd	xmm14, xmm3
	sete	dl
	or	dl, al
	pshufd	xmm2, xmm14, 238                # xmm2 = xmm14[2,3,2,3]
	ucomisd	xmm2, xmm3
	sete	al
	or	al, dl
	or	al, cl
	cmpltpd	xmm14, xmmword ptr [rip + .LCPI436_18]
	xorpd	xmm2, xmm2
	jne	.LBB436_25
# %bb.24:                               #   in Loop: Header=BB436_10 Depth=1
	xorps	xmm2, xmm2
	cvtsd2ss	xmm2, xmm4
	jmp	.LBB436_25
	.p2align	4
.LBB436_15:                             #   in Loop: Header=BB436_10 Depth=1
	cvtss2sd	xmm3, xmm3
	mov	eax, dword ptr [rsi + 376]
	movapd	xmm8, xmm15
	test	eax, eax
	jne	.LBB436_18
	jmp	.LBB436_19
.LBB436_26:
	movsd	qword ptr [rsi + 320], xmm10
	movsd	qword ptr [rsi + 352], xmm13
	movsd	qword ptr [rsi + 360], xmm12
	movsd	qword ptr [rsi + 368], xmm11
	movupd	xmmword ptr [rsi + 336], xmm14
	movaps	xmm6, xmmword ptr [rsp + 128]   # 16-byte Reload
	movaps	xmm7, xmmword ptr [rsp + 144]   # 16-byte Reload
	movaps	xmm8, xmmword ptr [rsp + 160]   # 16-byte Reload
	movaps	xmm9, xmmword ptr [rsp + 176]   # 16-byte Reload
	movaps	xmm10, xmmword ptr [rsp + 192]  # 16-byte Reload
	movaps	xmm11, xmmword ptr [rsp + 208]  # 16-byte Reload
	movaps	xmm12, xmmword ptr [rsp + 224]  # 16-byte Reload
	movaps	xmm13, xmmword ptr [rsp + 240]  # 16-byte Reload
	movaps	xmm14, xmmword ptr [rsp + 256]  # 16-byte Reload
	movaps	xmm15, xmmword ptr [rsp + 272]  # 16-byte Reload
	add	rsp, 296
	pop	rbx
	pop	rdi
	pop	rsi
	pop	r12
	pop	r14
	pop	r15
	ret
.LBB436_27:
	movapd	xmm0, xmm6
	movaps	xmm6, xmmword ptr [rsp + 128]   # 16-byte Reload
	movaps	xmm7, xmmword ptr [rsp + 144]   # 16-byte Reload
	movaps	xmm8, xmmword ptr [rsp + 160]   # 16-byte Reload
	movaps	xmm9, xmmword ptr [rsp + 176]   # 16-byte Reload
	movaps	xmm10, xmmword ptr [rsp + 192]  # 16-byte Reload
	movaps	xmm11, xmmword ptr [rsp + 208]  # 16-byte Reload
	movaps	xmm12, xmmword ptr [rsp + 224]  # 16-byte Reload
	movaps	xmm13, xmmword ptr [rsp + 240]  # 16-byte Reload
	movaps	xmm14, xmmword ptr [rsp + 256]  # 16-byte Reload
	movaps	xmm15, xmmword ptr [rsp + 272]  # 16-byte Reload
	add	rsp, 296
	pop	rbx
	pop	rdi
	pop	rsi
	pop	r12
	pop	r14
	pop	r15
	jmp	exp                             # TAILCALL
.LBB436_2:
	movapd	xmm2, xmm15
	subsd	xmm2, xmm0
	cvtsd2ss	xmm2, xmm2
	movsd	xmm0, qword ptr [rsi + 224]     # xmm0 = mem[0],zero
	shufps	xmm2, xmm2, 0                   # xmm2 = xmm2[0,0,0,0]
	xor	eax, eax
	jmp	.LBB436_3
	.p2align	4
.LBB436_5:                              #   in Loop: Header=BB436_3 Depth=1
	movss	dword ptr [rsi + 4*rax + 1416], xmm3
	inc	rax
	cmp	rbx, rax
	je	.LBB436_9
.LBB436_3:                              # =>This Inner Loop Header: Depth=1
	movaps	xmm3, xmm0
	movaps	xmm0, xmm1
	subps	xmm0, xmm3
	mulps	xmm0, xmm2
	addps	xmm0, xmm3
	movaps	xmm3, xmm0
	shufps	xmm3, xmm0, 85                  # xmm3 = xmm3[1,1],xmm0[1,1]
	mulss	xmm3, dword ptr [rsi + 4*rax + 392]
	movd	ecx, xmm3
	and	ecx, 2147483647
	cmp	ecx, 2139095040
	jl	.LBB436_5
# %bb.4:                                #   in Loop: Header=BB436_3 Depth=1
	xorps	xmm3, xmm3
	jmp	.LBB436_5
	.seh_endproc
                                        # -- End function

_ZN9easyknack16HandwrittenFused7processERNS_14ProcessContextE: # @_ZN9easyknack16HandwrittenFused7processERNS_14ProcessContextE
.seh_proc _ZN9easyknack16HandwrittenFused7processERNS_14ProcessContextE
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
	sub	rsp, 280
	.seh_stackalloc 280
	movapd	xmmword ptr [rsp + 256], xmm15  # 16-byte Spill
	.seh_savexmm xmm15, 256
	movapd	xmmword ptr [rsp + 240], xmm14  # 16-byte Spill
	.seh_savexmm xmm14, 240
	movapd	xmmword ptr [rsp + 224], xmm13  # 16-byte Spill
	.seh_savexmm xmm13, 224
	movapd	xmmword ptr [rsp + 208], xmm12  # 16-byte Spill
	.seh_savexmm xmm12, 208
	movapd	xmmword ptr [rsp + 192], xmm11  # 16-byte Spill
	.seh_savexmm xmm11, 192
	movaps	xmmword ptr [rsp + 176], xmm10  # 16-byte Spill
	.seh_savexmm xmm10, 176
	movapd	xmmword ptr [rsp + 160], xmm9   # 16-byte Spill
	.seh_savexmm xmm9, 160
	movapd	xmmword ptr [rsp + 144], xmm8   # 16-byte Spill
	.seh_savexmm xmm8, 144
	movapd	xmmword ptr [rsp + 128], xmm7   # 16-byte Spill
	.seh_savexmm xmm7, 128
	movapd	xmmword ptr [rsp + 112], xmm6   # 16-byte Spill
	.seh_savexmm xmm6, 112
	.seh_endprologue
	mov	rsi, rdx
	movsd	xmm1, qword ptr [rdx + 32]      # xmm1 = mem[0],zero
	mulsd	xmm1, qword ptr [rip + .LCPI444_0]
	mov	rdi, rcx
	movsd	xmm0, qword ptr [rip + .LCPI444_1] # xmm0 = [-1.0E+0,0.0E+0]
	divsd	xmm0, xmm1
	call	exp
	mov	qword ptr [rsp + 64], rsi       # 8-byte Spill
	mov	rbx, qword ptr [rsi + 24]
	test	rbx, rbx
	je	.LBB444_10
# %bb.1:
	movsd	xmm15, qword ptr [rip + .LCPI444_2] # xmm15 = [1.0E+0,0.0E+0]
	movapd	xmm10, xmm15
	subsd	xmm10, xmm0
	movsd	xmm0, qword ptr [rdi + 304]     # xmm0 = mem[0],zero
	movsd	qword ptr [rsp + 56], xmm0      # 8-byte Spill
	movq	xmm2, qword ptr [rdi + 312]     # xmm2 = mem[0],zero
	movq	rax, xmm2
	movabs	r14, 9223372036854775807
	and	rax, r14
	movabs	r15, 9218868437227405312
	movsd	xmm0, qword ptr [rip + .LCPI444_3] # xmm0 = [3.0E+1,0.0E+0]
	movsd	xmm3, qword ptr [rip + .LCPI444_4] # xmm3 = [1.0000000000000001E-1,0.0E+0]
	cmp	rax, r15
	jl	.LBB444_2
# %bb.3:
	movsd	xmm0, qword ptr [rip + .LCPI444_5] # xmm0 = [7.0699999999999996E-1,0.0E+0]
	movapd	xmmword ptr [rsp + 96], xmm0    # 16-byte Spill
	jmp	.LBB444_4
.LBB444_2:
	minsd	xmm0, xmm2
	cmpltsd	xmm2, xmm3
	movapd	xmm1, xmm2
	andnpd	xmm1, xmm0
	andpd	xmm2, xmm3
	orpd	xmm2, xmm1
	movapd	xmmword ptr [rsp + 96], xmm2    # 16-byte Spill
.LBB444_4:
	xorps	xmm0, xmm0
	cvtsd2ss	xmm0, xmm10
	movss	dword ptr [rsp + 52], xmm0      # 4-byte Spill
	mov	rax, qword ptr [rdi + 144]
	mov	qword ptr [rsp + 80], rax       # 8-byte Spill
	mov	rax, qword ptr [rsp + 64]       # 8-byte Reload
	mov	rax, qword ptr [rax + 16]
	mov	qword ptr [rsp + 72], rax       # 8-byte Spill
	movsd	xmm7, qword ptr [rdi + 112]     # xmm7 = mem[0],zero
	movsd	xmm14, qword ptr [rdi + 128]    # xmm14 = mem[0],zero
	movupd	xmm13, xmmword ptr [rdi + 336]
	movsd	xmm0, qword ptr [rdi + 320]     # xmm0 = mem[0],zero
	movsd	qword ptr [rsp + 40], xmm0      # 8-byte Spill
	xor	ebp, ebp
	movsd	xmm2, qword ptr [rip + .LCPI444_7] # xmm2 = [5.0E-1,0.0E+0]
	movsd	xmm3, qword ptr [rip + .LCPI444_16] # xmm3 = [6.2831853071795862E+0,0.0E+0]
	movq	xmm0, qword ptr [rsp + 56]      # 8-byte Folded Reload
                                        # xmm0 = mem[0],zero
	movq	r12, xmm0
	and	r12, r14
	movsd	qword ptr [rsp + 88], xmm10     # 8-byte Spill
	jmp	.LBB444_5
	.p2align	4
.LBB444_43:                             #   in Loop: Header=BB444_5 Depth=1
	subsd	xmm6, xmm7
	movapd	xmm7, xmm6
	movsd	xmm3, qword ptr [rip + .LCPI444_16] # xmm3 = [6.2831853071795862E+0,0.0E+0]
	mulsd	xmm7, xmm3
	movzx	eax, al
	movd	xmm2, eax
	pshufd	xmm2, xmm2, 0                   # xmm2 = xmm2[0,0,0,0]
	shufps	xmm13, xmm13, 160               # xmm13 = xmm13[0,0,2,2]
	orps	xmm13, xmm2
	pslld	xmm13, 31
	psrad	xmm13, 31
	pandn	xmm13, xmm0
	mov	rax, qword ptr [rsp + 72]       # 8-byte Reload
	movss	dword ptr [rax + 4*rbp], xmm1
	inc	rbp
	cmp	rbx, rbp
	movapd	xmm15, xmm10
	movsd	xmm10, qword ptr [rsp + 88]     # 8-byte Reload
                                        # xmm10 = mem[0],zero
	movsd	xmm2, qword ptr [rip + .LCPI444_7] # xmm2 = [5.0E-1,0.0E+0]
	je	.LBB444_9
.LBB444_5:                              # =>This Inner Loop Header: Depth=1
	movapd	xmm0, xmm14
	movss	xmm1, dword ptr [rdi + 120]     # xmm1 = mem[0],zero,zero,zero
	xorps	xmm14, xmm14
	cvtss2sd	xmm14, xmm1
	subsd	xmm14, xmm0
	mulsd	xmm14, xmm10
	addsd	xmm14, xmm0
	movq	rax, xmm14
	and	rax, r14
	xorpd	xmm0, xmm0
	movapd	xmm6, xmm14
	cmp	rax, r15
	jl	.LBB444_7
# %bb.6:                                #   in Loop: Header=BB444_5 Depth=1
	xorpd	xmm6, xmm6
.LBB444_7:                              #   in Loop: Header=BB444_5 Depth=1
	mov	rax, qword ptr [rsp + 64]       # 8-byte Reload
	movsd	xmm11, qword ptr [rax + 32]     # xmm11 = mem[0],zero
	movsd	xmm1, qword ptr [rip + .LCPI444_6] # xmm1 = [2.0E+4,0.0E+0]
	minsd	xmm1, xmm6
	cmpltsd	xmm6, xmm0
	andnpd	xmm6, xmm1
	divsd	xmm6, xmm11
	mov	eax, dword ptr [rdi + 136]
	test	rax, rax
	je	.LBB444_8
# %bb.11:                               #   in Loop: Header=BB444_5 Depth=1
	movapd	xmm0, xmm6
	addsd	xmm0, xmm6
	ucomisd	xmm2, xmm0
	mov	ecx, 0
	jbe	.LBB444_21
# %bb.12:                               #   in Loop: Header=BB444_5 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI444_8]
	mov	ecx, 1
	ucomisd	xmm2, xmm0
	jbe	.LBB444_21
# %bb.13:                               #   in Loop: Header=BB444_5 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI444_9]
	mov	ecx, 2
	ucomisd	xmm2, xmm0
	jbe	.LBB444_21
# %bb.14:                               #   in Loop: Header=BB444_5 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI444_10]
	mov	ecx, 3
	ucomisd	xmm2, xmm0
	jbe	.LBB444_21
# %bb.15:                               #   in Loop: Header=BB444_5 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI444_11]
	mov	ecx, 4
	ucomisd	xmm2, xmm0
	jbe	.LBB444_21
# %bb.16:                               #   in Loop: Header=BB444_5 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI444_12]
	mov	ecx, 5
	ucomisd	xmm2, xmm0
	jbe	.LBB444_21
# %bb.17:                               #   in Loop: Header=BB444_5 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI444_13]
	mov	ecx, 6
	ucomisd	xmm2, xmm0
	jbe	.LBB444_21
# %bb.18:                               #   in Loop: Header=BB444_5 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI444_14]
	mov	ecx, 7
	ucomisd	xmm2, xmm0
	jbe	.LBB444_21
# %bb.19:                               #   in Loop: Header=BB444_5 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI444_15]
	mov	ecx, 8
	ucomisd	xmm2, xmm0
	jbe	.LBB444_21
# %bb.20:                               #   in Loop: Header=BB444_5 Depth=1
	mov	ecx, 9
	jmp	.LBB444_21
	.p2align	4
.LBB444_8:                              #   in Loop: Header=BB444_5 Depth=1
	xor	ecx, ecx
.LBB444_21:                             #   in Loop: Header=BB444_5 Depth=1
	imul	rax, rax, 81960
	mov	edx, ecx
	shl	edx, 13
	lea	r13d, [rdx + 4*rcx]
	add	r13, qword ptr [rsp + 80]       # 8-byte Folded Reload
	add	r13, rax
	divsd	xmm7, xmm3
	movapd	xmm0, xmm7
	call	floor
	movapd	xmm12, xmm7
	subsd	xmm12, xmm0
	mulsd	xmm12, qword ptr [rip + .LCPI444_17]
	cvttsd2si	rax, xmm12
	mov	rcx, rax
	sar	rcx, 63
	movapd	xmm0, xmm12
	subsd	xmm0, qword ptr [rip + .LCPI444_18]
	cvttsd2si	rsi, xmm0
	and	rsi, rcx
	or	rsi, rax
	movss	xmm0, dword ptr [r13 + 4*rsi]   # xmm0 = mem[0],zero,zero,zero
	cvtss2sd	xmm8, xmm0
	movq	rax, xmm8
	and	rax, r14
	cmp	rax, r15
	jl	.LBB444_23
# %bb.22:                               #   in Loop: Header=BB444_5 Depth=1
	xorps	xmm8, xmm8
.LBB444_23:                             #   in Loop: Header=BB444_5 Depth=1
	lea	eax, [rsi + 1]
	and	eax, 2047
	movss	xmm0, dword ptr [r13 + 4*rax]   # xmm0 = mem[0],zero,zero,zero
	cvtss2sd	xmm9, xmm0
	movq	rax, xmm9
	and	rax, r14
	cmp	rax, r15
	jl	.LBB444_25
# %bb.24:                               #   in Loop: Header=BB444_5 Depth=1
	xorps	xmm9, xmm9
.LBB444_25:                             #   in Loop: Header=BB444_5 Depth=1
	addsd	xmm6, xmm7
	movapd	xmm0, xmm6
	call	floor
	movapd	xmm7, xmm0
	movq	xmm1, rsi
	punpckldq	xmm1, xmmword ptr [rip + .LCPI444_19] # xmm1 = xmm1[0],mem[0],xmm1[1],mem[1]
	subpd	xmm1, xmmword ptr [rip + .LCPI444_20]
	movss	xmm2, dword ptr [rdi + 216]     # xmm2 = mem[0],zero,zero,zero
	cmp	rbx, 1
	jne	.LBB444_27
# %bb.26:                               #   in Loop: Header=BB444_5 Depth=1
	movss	dword ptr [rdi + 224], xmm2
	movss	xmm0, dword ptr [rdi + 220]     # xmm0 = mem[0],zero,zero,zero
	jmp	.LBB444_28
	.p2align	4
.LBB444_27:                             #   in Loop: Header=BB444_5 Depth=1
	movss	xmm0, dword ptr [rdi + 220]     # xmm0 = mem[0],zero,zero,zero
	movss	xmm3, dword ptr [rdi + 224]     # xmm3 = mem[0],zero,zero,zero
	subss	xmm2, xmm3
	movss	xmm4, dword ptr [rsp + 52]      # 4-byte Reload
                                        # xmm4 = mem[0],zero,zero,zero
	mulss	xmm2, xmm4
	addss	xmm2, xmm3
	movss	dword ptr [rdi + 224], xmm2
	movss	xmm2, dword ptr [rdi + 228]     # xmm2 = mem[0],zero,zero,zero
	subss	xmm0, xmm2
	mulss	xmm0, xmm4
	addss	xmm0, xmm2
.LBB444_28:                             #   in Loop: Header=BB444_5 Depth=1
	movapd	xmm2, xmm15
	minsd	xmm2, xmm8
	movsd	xmm4, qword ptr [rip + .LCPI444_1] # xmm4 = [-1.0E+0,0.0E+0]
	cmpltsd	xmm8, xmm4
	movapd	xmm3, xmm8
	andnpd	xmm3, xmm2
	andpd	xmm8, xmm4
	orpd	xmm8, xmm3
	movapd	xmm2, xmm15
	minsd	xmm2, xmm9
	cmpltsd	xmm9, xmm4
	movapd	xmm3, xmm9
	andnpd	xmm3, xmm2
	andpd	xmm9, xmm4
	orpd	xmm9, xmm3
	subsd	xmm9, xmm8
	movapd	xmm2, xmm1
	unpckhpd	xmm2, xmm1                      # xmm2 = xmm2[1],xmm1[1]
	addsd	xmm2, xmm1
	subsd	xmm12, xmm2
	mulsd	xmm12, xmm9
	addsd	xmm12, xmm8
	xorps	xmm8, xmm8
	cvtsd2ss	xmm8, xmm12
	mulss	xmm8, xmm0
	movd	eax, xmm8
	and	eax, 2147483647
	cmp	eax, 2139095040
	jl	.LBB444_30
# %bb.29:                               #   in Loop: Header=BB444_5 Depth=1
	xorps	xmm8, xmm8
.LBB444_30:                             #   in Loop: Header=BB444_5 Depth=1
	movss	dword ptr [rdi + 228], xmm0
	movapd	xmm1, xmm11
	mulsd	xmm1, qword ptr [rip + .LCPI444_21]
	cmp	r12, r15
	movsd	xmm3, qword ptr [rip + .LCPI444_22] # xmm3 = [5.0E+0,0.0E+0]
	jl	.LBB444_31
# %bb.32:                               #   in Loop: Header=BB444_5 Depth=1
	movsd	xmm0, qword ptr [rip + .LCPI444_23] # xmm0 = [1.0E+3,0.0E+0]
	jmp	.LBB444_33
	.p2align	4
.LBB444_31:                             #   in Loop: Header=BB444_5 Depth=1
	movapd	xmm2, xmm1
	movsd	xmm0, qword ptr [rsp + 56]      # 8-byte Reload
                                        # xmm0 = mem[0],zero
	minsd	xmm2, xmm0
	cmpltsd	xmm0, xmm3
	movapd	xmm3, xmm0
	andnpd	xmm3, xmm2
	movsd	xmm2, qword ptr [rip + .LCPI444_22] # xmm2 = [5.0E+0,0.0E+0]
	andpd	xmm0, xmm2
	orpd	xmm0, xmm3
	movsd	xmm3, qword ptr [rip + .LCPI444_22] # xmm3 = [5.0E+0,0.0E+0]
.LBB444_33:                             #   in Loop: Header=BB444_5 Depth=1
	movsd	xmm2, qword ptr [rsp + 40]      # 8-byte Reload
                                        # xmm2 = mem[0],zero
	subsd	xmm0, xmm2
	mulsd	xmm0, xmm10
	addsd	xmm2, xmm0
	movsd	xmm0, qword ptr [rdi + 328]     # xmm0 = mem[0],zero
	movapd	xmm12, xmmword ptr [rsp + 96]   # 16-byte Reload
	subsd	xmm12, xmm0
	mulsd	xmm12, xmm10
	addsd	xmm12, xmm0
	movsd	qword ptr [rdi + 328], xmm12
	minsd	xmm1, xmm2
	mulsd	xmm1, qword ptr [rip + .LCPI444_24]
	movsd	qword ptr [rsp + 40], xmm2      # 8-byte Spill
	movapd	xmm0, xmm2
	cmpltsd	xmm0, xmm3
	movapd	xmm2, xmm0
	andnpd	xmm2, xmm1
	movsd	xmm1, qword ptr [rip + .LCPI444_25] # xmm1 = [1.5707963267948966E+1,0.0E+0]
	andpd	xmm0, xmm1
	orpd	xmm0, xmm2
	divsd	xmm0, xmm11
	call	tan
	ucomisd	xmm12, qword ptr [rip + .LCPI444_3]
	movsd	xmm4, qword ptr [rip + .LCPI444_26] # xmm4 = [3.3333333333333333E-2,0.0E+0]
	ja	.LBB444_35
# %bb.34:                               #   in Loop: Header=BB444_5 Depth=1
	movapd	xmm4, xmm15
	divsd	xmm4, qword ptr [rdi + 328]
.LBB444_35:                             #   in Loop: Header=BB444_5 Depth=1
	movsd	xmm1, qword ptr [rip + .LCPI444_4] # xmm1 = [1.0000000000000001E-1,0.0E+0]
	cmpltsd	xmm12, xmm1
	mov	eax, dword ptr [rdi + 376]
	movapd	xmm1, xmm15
	test	eax, eax
	je	.LBB444_37
# %bb.36:                               #   in Loop: Header=BB444_5 Depth=1
	xorpd	xmm1, xmm1
.LBB444_37:                             #   in Loop: Header=BB444_5 Depth=1
	movsd	xmm2, qword ptr [rdi + 352]     # xmm2 = mem[0],zero
	movsd	xmm5, qword ptr [rdi + 360]     # xmm5 = mem[0],zero
	subsd	xmm1, xmm2
	mulsd	xmm1, xmm10
	addsd	xmm1, xmm2
	movsd	qword ptr [rdi + 352], xmm1
	movapd	xmm3, xmm15
	cmp	eax, 1
	je	.LBB444_39
# %bb.38:                               #   in Loop: Header=BB444_5 Depth=1
	xorpd	xmm3, xmm3
.LBB444_39:                             #   in Loop: Header=BB444_5 Depth=1
	xorps	xmm2, xmm2
	cvtss2sd	xmm2, xmm8
	movapd	xmm8, xmm12
	movsd	xmm9, qword ptr [rip + .LCPI444_27] # xmm9 = [1.0E+1,0.0E+0]
	andpd	xmm8, xmm9
	andnpd	xmm12, xmm4
	orpd	xmm12, xmm8
	movapd	xmm4, xmm0
	addsd	xmm4, xmm12
	mulsd	xmm4, xmm0
	addsd	xmm4, xmm15
	movapd	xmm9, xmm13
	unpckhpd	xmm9, xmm13                     # xmm9 = xmm9[1],xmm13[1]
	movaps	xmm8, xmm2
	subsd	xmm8, xmm9
	subsd	xmm3, xmm5
	mulsd	xmm3, xmm10
	addsd	xmm3, xmm5
	movsd	qword ptr [rdi + 360], xmm3
	movapd	xmm5, xmm15
	cmp	eax, 2
	je	.LBB444_41
# %bb.40:                               #   in Loop: Header=BB444_5 Depth=1
	xorpd	xmm5, xmm5
.LBB444_41:                             #   in Loop: Header=BB444_5 Depth=1
	movsd	xmm9, qword ptr [rdi + 368]     # xmm9 = mem[0],zero
	subsd	xmm5, xmm9
	mulsd	xmm5, xmm10
	addsd	xmm5, xmm9
	movapd	xmm10, xmm15
	movapd	xmm9, xmm15
	divsd	xmm9, xmm4
	movapd	xmm4, xmm0
	mulsd	xmm4, xmm9
	movapd	xmm11, xmm13
	mulsd	xmm11, xmm4
	movapd	xmm15, xmm13
	mulpd	xmm15, xmm9
	unpcklpd	xmm9, xmm11                     # xmm9 = xmm9[0],xmm11[0]
	addpd	xmm9, xmm13
	movsd	xmm9, xmm15                     # xmm9 = xmm15[0],xmm9[1]
	mulsd	xmm0, xmm4
	unpcklpd	xmm4, xmm0                      # xmm4 = xmm4[0],xmm0[0]
	unpcklpd	xmm8, xmm8                      # xmm8 = xmm8[0,0]
	mulpd	xmm4, xmm8
	addpd	xmm4, xmm9
	movapd	xmm0, xmm4
	addpd	xmm0, xmm4
	subpd	xmm0, xmm13
	mulsd	xmm12, xmm4
	subsd	xmm2, xmm12
	movapd	xmm8, xmm4
	unpckhpd	xmm8, xmm4                      # xmm8 = xmm8[1],xmm4[1]
	mulsd	xmm1, xmm8
	subsd	xmm2, xmm8
	mulsd	xmm2, xmm3
	addsd	xmm2, xmm1
	movsd	qword ptr [rdi + 368], xmm5
	mulsd	xmm4, xmm5
	addsd	xmm4, xmm2
	movq	rax, xmm4
	movapd	xmm1, xmm4
	movapd	xmm2, xmmword ptr [rip + .LCPI444_28] # xmm2 = [NaN,NaN]
	andpd	xmm1, xmm2
	and	rax, r14
	movabs	rcx, 9218868437227405311
	cmp	rax, rcx
	setg	al
	ucomisd	xmm1, qword ptr [rip + .LCPI444_29]
	seta	cl
	movapd	xmm13, xmm0
	andpd	xmm13, xmm2
	movsd	xmm2, qword ptr [rip + .LCPI444_30] # xmm2 = [+Inf,0.0E+0]
	ucomisd	xmm13, xmm2
	sete	dl
	or	dl, al
	pshufd	xmm1, xmm13, 238                # xmm1 = xmm13[2,3,2,3]
	ucomisd	xmm1, xmm2
	sete	al
	or	al, dl
	or	al, cl
	cmpltpd	xmm13, xmmword ptr [rip + .LCPI444_31]
	xorpd	xmm1, xmm1
	jne	.LBB444_43
# %bb.42:                               #   in Loop: Header=BB444_5 Depth=1
	xorps	xmm1, xmm1
	cvtsd2ss	xmm1, xmm4
	jmp	.LBB444_43
.LBB444_9:
	movsd	xmm0, qword ptr [rsp + 40]      # 8-byte Reload
                                        # xmm0 = mem[0],zero
	movsd	qword ptr [rdi + 320], xmm0
	movsd	qword ptr [rdi + 128], xmm14
	movsd	qword ptr [rdi + 112], xmm7
	movupd	xmmword ptr [rdi + 336], xmm13
.LBB444_10:
	movaps	xmm6, xmmword ptr [rsp + 112]   # 16-byte Reload
	movaps	xmm7, xmmword ptr [rsp + 128]   # 16-byte Reload
	movaps	xmm8, xmmword ptr [rsp + 144]   # 16-byte Reload
	movaps	xmm9, xmmword ptr [rsp + 160]   # 16-byte Reload
	movaps	xmm10, xmmword ptr [rsp + 176]  # 16-byte Reload
	movaps	xmm11, xmmword ptr [rsp + 192]  # 16-byte Reload
	movaps	xmm12, xmmword ptr [rsp + 208]  # 16-byte Reload
	movaps	xmm13, xmmword ptr [rsp + 224]  # 16-byte Reload
	movaps	xmm14, xmmword ptr [rsp + 240]  # 16-byte Reload
	movaps	xmm15, xmmword ptr [rsp + 256]  # 16-byte Reload
	add	rsp, 280
	pop	rbx
	pop	rbp
	pop	rdi
	pop	rsi
	pop	r12
	pop	r13
	pop	r14
	pop	r15
	ret
	.seh_endproc
                                        # -- End function

_ZN9easyknack19renderOscGainFilterILb1EEEvRNS_14OscillatorNodeERNS_8MathNodeERNS_10FilterNodeEPfS7_S7_yd: # @_ZN9easyknack19renderOscGainFilterILb1EEEvRNS_14OscillatorNodeERNS_8MathNodeERNS_10FilterNodeEPfS7_S7_yd
.seh_proc _ZN9easyknack19renderOscGainFilterILb1EEEvRNS_14OscillatorNodeERNS_8MathNodeERNS_10FilterNodeEPfS7_S7_yd
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
	sub	rsp, 264
	.seh_stackalloc 264
	movapd	xmmword ptr [rsp + 240], xmm15  # 16-byte Spill
	.seh_savexmm xmm15, 240
	movapd	xmmword ptr [rsp + 224], xmm14  # 16-byte Spill
	.seh_savexmm xmm14, 224
	movapd	xmmword ptr [rsp + 208], xmm13  # 16-byte Spill
	.seh_savexmm xmm13, 208
	movapd	xmmword ptr [rsp + 192], xmm12  # 16-byte Spill
	.seh_savexmm xmm12, 192
	movaps	xmmword ptr [rsp + 176], xmm11  # 16-byte Spill
	.seh_savexmm xmm11, 176
	movapd	xmmword ptr [rsp + 160], xmm10  # 16-byte Spill
	.seh_savexmm xmm10, 160
	movapd	xmmword ptr [rsp + 144], xmm9   # 16-byte Spill
	.seh_savexmm xmm9, 144
	movapd	xmmword ptr [rsp + 128], xmm8   # 16-byte Spill
	.seh_savexmm xmm8, 128
	movaps	xmmword ptr [rsp + 112], xmm7   # 16-byte Spill
	.seh_savexmm xmm7, 112
	movapd	xmmword ptr [rsp + 96], xmm6    # 16-byte Spill
	.seh_savexmm xmm6, 96
	.seh_endprologue
	mov	rsi, r8
	mov	rdi, rdx
	mov	rbx, rcx
	movsd	xmm7, qword ptr [rsp + 392]     # xmm7 = mem[0],zero
	movsd	xmm1, qword ptr [rip + .LCPI445_0] # xmm1 = [5.0000000000000001E-3,0.0E+0]
	mulsd	xmm1, xmm7
	movsd	xmm0, qword ptr [rip + .LCPI445_1] # xmm0 = [-1.0E+0,0.0E+0]
	divsd	xmm0, xmm1
	call	exp
	cmp	qword ptr [rsp + 384], 0
	je	.LBB445_7
# %bb.1:
	movsd	xmm10, qword ptr [rip + .LCPI445_2] # xmm10 = [1.0E+0,0.0E+0]
	movapd	xmm11, xmm10
	subsd	xmm11, xmm0
	xorps	xmm0, xmm0
	cvtsd2ss	xmm0, xmm11
	movss	dword ptr [rsp + 36], xmm0      # 4-byte Spill
	mov	rax, qword ptr [rbx + 88]
	mov	qword ptr [rsp + 40], rax       # 8-byte Spill
	movsd	xmm3, qword ptr [rip + .LCPI445_3] # xmm3 = [4.5000000000000001E-1,0.0E+0]
	movups	xmm0, xmmword ptr [rsi + 56]
	shufps	xmm0, xmm0, 78                  # xmm0 = xmm0[2,3,0,1]
	movaps	xmm4, xmm0
	shufps	xmm4, xmm0, 245                 # xmm4 = xmm4[1,1],xmm0[3,3]
	andps	xmm4, xmmword ptr [rip + .LCPI445_4]
	pcmpgtd	xmm4, xmmword ptr [rip + .LCPI445_5]
	mulsd	xmm3, xmm7
	movapd	xmm1, xmmword ptr [rip + .LCPI445_6] # xmm1 = [1.0000000000000001E-1,5.0E+0]
	movapd	xmm2, xmmword ptr [rip + .LCPI445_7] # xmm2 = [3.0E+1,u]
	movapd	xmmword ptr [rsp + 80], xmm3    # 16-byte Spill
	unpcklpd	xmm2, xmm3                      # xmm2 = xmm2[0],xmm3[0]
	minpd	xmm2, xmm0
	cmpltpd	xmm0, xmm1
	andpd	xmm1, xmm0
	andnpd	xmm0, xmm2
	orpd	xmm0, xmm1
	movupd	xmm12, xmmword ptr [rsi + 88]
	movdqa	xmm1, xmm4
	pandn	xmm1, xmm0
	pand	xmm4, xmmword ptr [rip + .LCPI445_8]
	por	xmm4, xmm1
	movdqa	xmmword ptr [rsp + 64], xmm4    # 16-byte Spill
	movsd	xmm8, qword ptr [rsi + 72]      # xmm8 = mem[0],zero
	movapd	xmm0, xmm11
	unpcklpd	xmm0, xmm11                     # xmm0 = xmm0[0],xmm11[0]
	movapd	xmmword ptr [rsp + 48], xmm0    # 16-byte Spill
	xor	r13d, r13d
	movabs	rbp, 9223372036854775807
	movabs	r15, 9218868437227405312
	movsd	xmm9, qword ptr [rip + .LCPI445_10] # xmm9 = [5.0E-1,0.0E+0]
	jmp	.LBB445_2
	.p2align	4
.LBB445_39:                             #   in Loop: Header=BB445_2 Depth=1
	movzx	eax, al
	movd	xmm2, eax
	pshufd	xmm2, xmm2, 0                   # xmm2 = xmm2[0,0,0,0]
	shufps	xmm12, xmm12, 160               # xmm12 = xmm12[0,0,2,2]
	orps	xmm12, xmm2
	pslld	xmm12, 31
	psrad	xmm12, 31
	pandn	xmm12, xmm0
	mov	rax, qword ptr [rsp + 376]
	movss	dword ptr [rax + 4*r13], xmm1
	inc	r13
	cmp	r14, r13
	movsd	xmm7, qword ptr [rsp + 392]     # xmm7 = mem[0],zero
	je	.LBB445_6
.LBB445_2:                              # =>This Inner Loop Header: Depth=1
	movss	xmm0, dword ptr [rbx + 64]      # xmm0 = mem[0],zero,zero,zero
	xorps	xmm6, xmm6
	cvtss2sd	xmm6, xmm0
	movsd	xmm0, qword ptr [rbx + 72]      # xmm0 = mem[0],zero
	subsd	xmm6, xmm0
	mulsd	xmm6, xmm11
	addsd	xmm6, xmm0
	movq	rax, xmm6
	movsd	qword ptr [rbx + 72], xmm6
	and	rax, rbp
	xorpd	xmm0, xmm0
	cmp	rax, r15
	jl	.LBB445_4
# %bb.3:                                #   in Loop: Header=BB445_2 Depth=1
	xorpd	xmm6, xmm6
.LBB445_4:                              #   in Loop: Header=BB445_2 Depth=1
	movsd	xmm1, qword ptr [rip + .LCPI445_9] # xmm1 = [2.0E+4,0.0E+0]
	minsd	xmm1, xmm6
	cmpltsd	xmm6, xmm0
	andnpd	xmm6, xmm1
	divsd	xmm6, xmm7
	mov	eax, dword ptr [rbx + 80]
	test	rax, rax
	je	.LBB445_5
# %bb.8:                                #   in Loop: Header=BB445_2 Depth=1
	movapd	xmm0, xmm6
	addsd	xmm0, xmm6
	ucomisd	xmm9, xmm0
	mov	ecx, 0
	jbe	.LBB445_18
# %bb.9:                                #   in Loop: Header=BB445_2 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI445_11]
	mov	ecx, 1
	ucomisd	xmm9, xmm0
	jbe	.LBB445_18
# %bb.10:                               #   in Loop: Header=BB445_2 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI445_12]
	mov	ecx, 2
	ucomisd	xmm9, xmm0
	jbe	.LBB445_18
# %bb.11:                               #   in Loop: Header=BB445_2 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI445_13]
	mov	ecx, 3
	ucomisd	xmm9, xmm0
	jbe	.LBB445_18
# %bb.12:                               #   in Loop: Header=BB445_2 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI445_14]
	mov	ecx, 4
	ucomisd	xmm9, xmm0
	jbe	.LBB445_18
# %bb.13:                               #   in Loop: Header=BB445_2 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI445_15]
	mov	ecx, 5
	ucomisd	xmm9, xmm0
	jbe	.LBB445_18
# %bb.14:                               #   in Loop: Header=BB445_2 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI445_16]
	mov	ecx, 6
	ucomisd	xmm9, xmm0
	jbe	.LBB445_18
# %bb.15:                               #   in Loop: Header=BB445_2 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI445_17]
	mov	ecx, 7
	ucomisd	xmm9, xmm0
	jbe	.LBB445_18
# %bb.16:                               #   in Loop: Header=BB445_2 Depth=1
	movapd	xmm0, xmm6
	mulsd	xmm0, qword ptr [rip + .LCPI445_18]
	mov	ecx, 8
	ucomisd	xmm9, xmm0
	jbe	.LBB445_18
# %bb.17:                               #   in Loop: Header=BB445_2 Depth=1
	mov	ecx, 9
	jmp	.LBB445_18
	.p2align	4
.LBB445_5:                              #   in Loop: Header=BB445_2 Depth=1
	xor	ecx, ecx
.LBB445_18:                             #   in Loop: Header=BB445_2 Depth=1
	imul	rax, rax, 81960
	mov	edx, ecx
	shl	edx, 13
	lea	r14d, [rdx + 4*rcx]
	add	r14, qword ptr [rsp + 40]       # 8-byte Folded Reload
	add	r14, rax
	movsd	xmm13, qword ptr [rbx + 56]     # xmm13 = mem[0],zero
	divsd	xmm13, qword ptr [rip + .LCPI445_19]
	movapd	xmm0, xmm13
	call	floor
	movapd	xmm7, xmm13
	subsd	xmm7, xmm0
	mulsd	xmm7, qword ptr [rip + .LCPI445_20]
	cvttsd2si	rax, xmm7
	mov	rcx, rax
	sar	rcx, 63
	movapd	xmm0, xmm7
	subsd	xmm0, qword ptr [rip + .LCPI445_21]
	cvttsd2si	r12, xmm0
	and	r12, rcx
	or	r12, rax
	movss	xmm0, dword ptr [r14 + 4*r12]   # xmm0 = mem[0],zero,zero,zero
	cvtss2sd	xmm15, xmm0
	movq	rax, xmm15
	and	rax, rbp
	cmp	rax, r15
	jl	.LBB445_20
# %bb.19:                               #   in Loop: Header=BB445_2 Depth=1
	xorps	xmm15, xmm15
.LBB445_20:                             #   in Loop: Header=BB445_2 Depth=1
	lea	eax, [r12 + 1]
	and	eax, 2047
	movss	xmm0, dword ptr [r14 + 4*rax]   # xmm0 = mem[0],zero,zero,zero
	cvtss2sd	xmm14, xmm0
	movq	rax, xmm14
	and	rax, rbp
	cmp	rax, r15
	jl	.LBB445_22
# %bb.21:                               #   in Loop: Header=BB445_2 Depth=1
	xorps	xmm14, xmm14
.LBB445_22:                             #   in Loop: Header=BB445_2 Depth=1
	addsd	xmm6, xmm13
	movapd	xmm0, xmm6
	call	floor
	movq	xmm1, r12
	punpckldq	xmm1, xmmword ptr [rip + .LCPI445_22] # xmm1 = xmm1[0],mem[0],xmm1[1],mem[1]
	subpd	xmm1, xmmword ptr [rip + .LCPI445_23]
	subsd	xmm6, xmm0
	mulsd	xmm6, qword ptr [rip + .LCPI445_19]
	movsd	qword ptr [rbx + 56], xmm6
	movss	xmm2, dword ptr [rdi + 56]      # xmm2 = mem[0],zero,zero,zero
	mov	r14, qword ptr [rsp + 384]
	cmp	r14, 1
	jne	.LBB445_24
# %bb.23:                               #   in Loop: Header=BB445_2 Depth=1
	movss	dword ptr [rdi + 64], xmm2
	movss	xmm0, dword ptr [rdi + 60]      # xmm0 = mem[0],zero,zero,zero
	jmp	.LBB445_25
	.p2align	4
.LBB445_24:                             #   in Loop: Header=BB445_2 Depth=1
	movss	xmm0, dword ptr [rdi + 60]      # xmm0 = mem[0],zero,zero,zero
	movss	xmm3, dword ptr [rdi + 64]      # xmm3 = mem[0],zero,zero,zero
	subss	xmm2, xmm3
	movss	xmm4, dword ptr [rsp + 36]      # 4-byte Reload
                                        # xmm4 = mem[0],zero,zero,zero
	mulss	xmm2, xmm4
	addss	xmm2, xmm3
	movss	dword ptr [rdi + 64], xmm2
	movss	xmm2, dword ptr [rdi + 68]      # xmm2 = mem[0],zero,zero,zero
	subss	xmm0, xmm2
	mulss	xmm0, xmm4
	addss	xmm0, xmm2
.LBB445_25:                             #   in Loop: Header=BB445_2 Depth=1
	movapd	xmm2, xmm10
	minsd	xmm2, xmm15
	movsd	xmm4, qword ptr [rip + .LCPI445_1] # xmm4 = [-1.0E+0,0.0E+0]
	cmpltsd	xmm15, xmm4
	movapd	xmm3, xmm15
	andnpd	xmm3, xmm2
	andpd	xmm15, xmm4
	orpd	xmm15, xmm3
	movapd	xmm2, xmm10
	minsd	xmm2, xmm14
	cmpltsd	xmm14, xmm4
	movapd	xmm3, xmm14
	andnpd	xmm3, xmm2
	andpd	xmm14, xmm4
	orpd	xmm14, xmm3
	subsd	xmm14, xmm15
	movapd	xmm2, xmm1
	unpckhpd	xmm2, xmm1                      # xmm2 = xmm2[1],xmm1[1]
	addsd	xmm2, xmm1
	subsd	xmm7, xmm2
	mulsd	xmm7, xmm14
	addsd	xmm7, xmm15
	cvtsd2ss	xmm7, xmm7
	movss	dword ptr [rdi + 68], xmm0
	mulss	xmm7, xmm0
	movd	eax, xmm7
	and	eax, 2147483647
	cmp	eax, 2139095040
	jl	.LBB445_27
# %bb.26:                               #   in Loop: Header=BB445_2 Depth=1
	xorps	xmm7, xmm7
.LBB445_27:                             #   in Loop: Header=BB445_2 Depth=1
	movupd	xmm0, xmmword ptr [rsi + 80]
	unpcklpd	xmm0, xmm8                      # xmm0 = xmm0[0],xmm8[0]
	movapd	xmm6, xmmword ptr [rsp + 64]    # 16-byte Reload
	subpd	xmm6, xmm0
	mulpd	xmm6, xmmword ptr [rsp + 48]    # 16-byte Folded Reload
	addpd	xmm6, xmm0
	movlpd	qword ptr [rsi + 80], xmm6
	movapd	xmm8, xmm6
	unpckhpd	xmm8, xmm6                      # xmm8 = xmm8[1],xmm6[1]
	movapd	xmm1, xmmword ptr [rsp + 80]    # 16-byte Reload
	minsd	xmm1, xmm8
	mulsd	xmm1, qword ptr [rip + .LCPI445_24]
	movapd	xmm0, xmm6
	cmpltpd	xmm0, xmmword ptr [rip + .LCPI445_6]
	pextrw	eax, xmm0, 4
	movsd	xmm0, qword ptr [rip + .LCPI445_25] # xmm0 = [1.5707963267948966E+1,0.0E+0]
	test	al, 1
	jne	.LBB445_29
# %bb.28:                               #   in Loop: Header=BB445_2 Depth=1
	movapd	xmm0, xmm1
.LBB445_29:                             #   in Loop: Header=BB445_2 Depth=1
	divsd	xmm0, qword ptr [rsp + 392]
	call	tan
	ucomisd	xmm6, qword ptr [rip + .LCPI445_26]
	movsd	xmm4, qword ptr [rip + .LCPI445_27] # xmm4 = [3.3333333333333333E-2,0.0E+0]
	ja	.LBB445_31
# %bb.30:                               #   in Loop: Header=BB445_2 Depth=1
	movapd	xmm4, xmm10
	divsd	xmm4, qword ptr [rsi + 80]
.LBB445_31:                             #   in Loop: Header=BB445_2 Depth=1
	cmpltsd	xmm6, qword ptr [rip + .LCPI445_28]
	mov	eax, dword ptr [rsi + 128]
	movapd	xmm1, xmm10
	test	eax, eax
	je	.LBB445_33
# %bb.32:                               #   in Loop: Header=BB445_2 Depth=1
	xorpd	xmm1, xmm1
.LBB445_33:                             #   in Loop: Header=BB445_2 Depth=1
	movsd	xmm2, qword ptr [rsi + 104]     # xmm2 = mem[0],zero
	movsd	xmm5, qword ptr [rsi + 112]     # xmm5 = mem[0],zero
	subsd	xmm1, xmm2
	mulsd	xmm1, xmm11
	addsd	xmm1, xmm2
	movsd	qword ptr [rsi + 104], xmm1
	movapd	xmm3, xmm10
	cmp	eax, 1
	je	.LBB445_35
# %bb.34:                               #   in Loop: Header=BB445_2 Depth=1
	xorpd	xmm3, xmm3
.LBB445_35:                             #   in Loop: Header=BB445_2 Depth=1
	xorps	xmm2, xmm2
	cvtss2sd	xmm2, xmm7
	movapd	xmm7, xmm6
	movsd	xmm13, qword ptr [rip + .LCPI445_29] # xmm13 = [1.0E+1,0.0E+0]
	andpd	xmm7, xmm13
	andnpd	xmm6, xmm4
	orpd	xmm6, xmm7
	movapd	xmm4, xmm0
	addsd	xmm4, xmm6
	mulsd	xmm4, xmm0
	addsd	xmm4, xmm10
	movapd	xmm13, xmm12
	unpckhpd	xmm13, xmm12                    # xmm13 = xmm13[1],xmm12[1]
	movaps	xmm7, xmm2
	subsd	xmm7, xmm13
	subsd	xmm3, xmm5
	mulsd	xmm3, xmm11
	addsd	xmm3, xmm5
	movsd	qword ptr [rsi + 112], xmm3
	movapd	xmm5, xmm10
	cmp	eax, 2
	je	.LBB445_37
# %bb.36:                               #   in Loop: Header=BB445_2 Depth=1
	xorpd	xmm5, xmm5
.LBB445_37:                             #   in Loop: Header=BB445_2 Depth=1
	movsd	xmm13, qword ptr [rsi + 120]    # xmm13 = mem[0],zero
	subsd	xmm5, xmm13
	mulsd	xmm5, xmm11
	addsd	xmm5, xmm13
	movapd	xmm13, xmm10
	divsd	xmm13, xmm4
	movapd	xmm4, xmm0
	mulsd	xmm4, xmm13
	movapd	xmm14, xmm12
	mulsd	xmm14, xmm4
	movapd	xmm15, xmm12
	mulpd	xmm15, xmm13
	unpcklpd	xmm13, xmm14                    # xmm13 = xmm13[0],xmm14[0]
	addpd	xmm13, xmm12
	movsd	xmm13, xmm15                    # xmm13 = xmm15[0],xmm13[1]
	mulsd	xmm0, xmm4
	unpcklpd	xmm4, xmm0                      # xmm4 = xmm4[0],xmm0[0]
	unpcklpd	xmm7, xmm7                      # xmm7 = xmm7[0,0]
	mulpd	xmm4, xmm7
	addpd	xmm4, xmm13
	movapd	xmm0, xmm4
	addpd	xmm0, xmm4
	subpd	xmm0, xmm12
	mulsd	xmm6, xmm4
	subsd	xmm2, xmm6
	movapd	xmm6, xmm4
	unpckhpd	xmm6, xmm4                      # xmm6 = xmm6[1],xmm4[1]
	mulsd	xmm1, xmm6
	subsd	xmm2, xmm6
	mulsd	xmm2, xmm3
	addsd	xmm2, xmm1
	movsd	qword ptr [rsi + 120], xmm5
	mulsd	xmm4, xmm5
	addsd	xmm4, xmm2
	movq	rax, xmm4
	movapd	xmm1, xmm4
	movapd	xmm2, xmmword ptr [rip + .LCPI445_30] # xmm2 = [NaN,NaN]
	andpd	xmm1, xmm2
	and	rax, rbp
	movabs	rcx, 9218868437227405311
	cmp	rax, rcx
	setg	al
	ucomisd	xmm1, qword ptr [rip + .LCPI445_31]
	seta	cl
	movapd	xmm12, xmm0
	andpd	xmm12, xmm2
	movsd	xmm2, qword ptr [rip + .LCPI445_32] # xmm2 = [+Inf,0.0E+0]
	ucomisd	xmm12, xmm2
	sete	dl
	or	dl, al
	pshufd	xmm1, xmm12, 238                # xmm1 = xmm12[2,3,2,3]
	ucomisd	xmm1, xmm2
	sete	al
	or	al, dl
	or	al, cl
	cmpltpd	xmm12, xmmword ptr [rip + .LCPI445_33]
	xorpd	xmm1, xmm1
	jne	.LBB445_39
# %bb.38:                               #   in Loop: Header=BB445_2 Depth=1
	xorps	xmm1, xmm1
	cvtsd2ss	xmm1, xmm4
	jmp	.LBB445_39
.LBB445_6:
	movsd	qword ptr [rsi + 72], xmm8
	movupd	xmmword ptr [rsi + 88], xmm12
.LBB445_7:
	movaps	xmm6, xmmword ptr [rsp + 96]    # 16-byte Reload
	movaps	xmm7, xmmword ptr [rsp + 112]   # 16-byte Reload
	movaps	xmm8, xmmword ptr [rsp + 128]   # 16-byte Reload
	movaps	xmm9, xmmword ptr [rsp + 144]   # 16-byte Reload
	movaps	xmm10, xmmword ptr [rsp + 160]  # 16-byte Reload
	movaps	xmm11, xmmword ptr [rsp + 176]  # 16-byte Reload
	movaps	xmm12, xmmword ptr [rsp + 192]  # 16-byte Reload
	movaps	xmm13, xmmword ptr [rsp + 208]  # 16-byte Reload
	movaps	xmm14, xmmword ptr [rsp + 224]  # 16-byte Reload
	movaps	xmm15, xmmword ptr [rsp + 240]  # 16-byte Reload
	add	rsp, 264
	pop	rbx
	pop	rbp
	pop	rdi
	pop	rsi
	pop	r12
	pop	r13
	pop	r14
	pop	r15
	ret
	.seh_endproc
                                        # -- End function