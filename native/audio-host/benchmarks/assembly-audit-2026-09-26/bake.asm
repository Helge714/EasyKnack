00000001400b6920 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)>:
1400b6920:     	pushq	%r15
1400b6922:     	pushq	%r14
1400b6924:     	pushq	%r13
1400b6926:     	pushq	%r12
1400b6928:     	pushq	%rsi
1400b6929:     	pushq	%rdi
1400b692a:     	pushq	%rbp
1400b692b:     	pushq	%rbx
1400b692c:     	subq	$0xf8, %rsp
1400b6933:     	movapd	%xmm15, 0xe0(%rsp)
1400b693d:     	movapd	%xmm14, 0xd0(%rsp)
1400b6947:     	movaps	%xmm13, 0xc0(%rsp)
1400b6950:     	movaps	%xmm12, 0xb0(%rsp)
1400b6959:     	movaps	%xmm11, 0xa0(%rsp)
1400b6962:     	movaps	%xmm10, 0x90(%rsp)
1400b696b:     	movaps	%xmm9, 0x80(%rsp)
1400b6974:     	movaps	%xmm8, 0x70(%rsp)
1400b697a:     	movaps	%xmm7, 0x60(%rsp)
1400b697f:     	movdqa	%xmm6, 0x50(%rsp)
1400b6985:     	movq	%r8, %rsi
1400b6988:     	movq	%rdx, %r15
1400b698b:     	movq	%rcx, %rbx
1400b698e:     	movq	0x30(%r8), %rdi
1400b6992:     	testq	%rdi, %rdi
1400b6995:     	jne	0x1400b699b <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x7b>
1400b6997:     	movq	0x18(%rsi), %rdi
1400b699b:     	movq	0x38(%r15), %rax
1400b699f:     	xorl	%ebp, %ebp
1400b69a1:     	movq	%rax, 0x28(%rsp)
1400b69a6:     	testq	%rax, %rax
1400b69a9:     	je	0x1400b6bd0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x2b0>
1400b69af:     	cmpq	$0x1, 0x58(%r15)
1400b69b4:     	jne	0x1400b6bd0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x2b0>
1400b69ba:     	cmpq	0x60(%r15), %rdi
1400b69be:     	ja	0x1400b6bd0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x2b0>
1400b69c4:     	cmpq	$0x0, 0x40(%rsi)
1400b69c9:     	jne	0x1400b6bd0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x2b0>
1400b69cf:     	cvtsi2ssl	0x4c(%r15), %xmm0
1400b69d5:     	movq	0x8(%rsi), %r14
1400b69d9:     	testq	%r14, %r14
1400b69dc:     	je	0x1400b69ed <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xcd>
1400b69de:     	movq	(%rsi), %rax
1400b69e1:     	movq	(%rax), %rax
1400b69e4:     	testq	%rax, %rax
1400b69e7:     	je	0x1400b69ed <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xcd>
1400b69e9:     	movss	(%rax), %xmm0
1400b69ed:     	movd	%xmm0, %eax
1400b69f1:     	andl	$0x7fffffff, %eax       # imm = 0x7FFFFFFF
1400b69f6:     	cmpl	$0x7f800000, %eax       # imm = 0x7F800000
1400b69fb:     	jl	0x1400b6a0f <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xef>
1400b69fd:     	movl	0x48(%r15), %eax
1400b6a01:     	movl	$0x0, 0x20(%rsp)
1400b6a09:     	testl	%eax, %eax
1400b6a0b:     	jg	0x1400b6a37 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x117>
1400b6a0d:     	jmp	0x1400b6a88 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x168>
1400b6a0f:     	callq	0x140171a50 <floorf>
1400b6a14:     	movl	0x48(%r15), %eax
1400b6a18:     	cvtsi2ss	%eax, %xmm1
1400b6a1c:     	minss	%xmm0, %xmm1
1400b6a20:     	xorps	%xmm2, %xmm2
1400b6a23:     	cmpltss	%xmm2, %xmm0
1400b6a28:     	andnps	%xmm1, %xmm0
1400b6a2b:     	cvttss2si	%xmm0, %ecx
1400b6a2f:     	movl	%ecx, 0x20(%rsp)
1400b6a33:     	testl	%eax, %eax
1400b6a35:     	jle	0x1400b6a88 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x168>
1400b6a37:     	movq	0x28(%rsp), %rcx
1400b6a3c:     	cmpb	$0x1, 0xc0(%rcx)
1400b6a43:     	jne	0x1400b6a88 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x168>
1400b6a45:     	movq	0x28(%rsp), %rcx
1400b6a4a:     	movq	0x18(%rcx), %rcx
1400b6a4e:     	movslq	0x20(%rsp), %rdx
1400b6a53:     	movl	%eax, %eax
1400b6a55:     	xorl	%r8d, %r8d
1400b6a58:     	movss	0xc1c40(%rip), %xmm0    # 0x1401786a0 <.refptr._newmode+0x5618>
1400b6a60:     	movaps	%xmm0, %xmm1
1400b6a63:     	cmpq	%rdx, %r8
1400b6a66:     	jl	0x1400b6a6b <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x14b>
1400b6a68:     	xorps	%xmm1, %xmm1
1400b6a6b:     	movss	(%rcx,%r8,4), %xmm2
1400b6a71:     	ucomiss	%xmm1, %xmm2
1400b6a74:     	jne	0x1400b6bce <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x2ae>
1400b6a7a:     	jp	0x1400b6bce <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x2ae>
1400b6a80:     	incq	%r8
1400b6a83:     	cmpq	%r8, %rax
1400b6a86:     	jne	0x1400b6a60 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x140>
1400b6a88:     	cmpq	$0x9, %r14
1400b6a8c:     	movl	$0x9, %eax
1400b6a91:     	cmovbq	%r14, %rax
1400b6a95:     	movb	$0x0, 0x40(%rsp)
1400b6a9a:     	movq	$0x0, 0x38(%rsp)
1400b6aa3:     	testq	%r14, %r14
1400b6aa6:     	je	0x1400b6b28 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x208>
1400b6aac:     	movq	0x80(%rsi), %rcx
1400b6ab3:     	movq	(%rsi), %rdx
1400b6ab6:     	movq	0x70(%rsi), %r8
1400b6aba:     	xorl	%r9d, %r9d
1400b6abd:     	jmp	0x1400b6ac8 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x1a8>
1400b6abf:     	nop
1400b6ac0:     	incq	%r9
1400b6ac3:     	cmpq	%r9, %rax
1400b6ac6:     	je	0x1400b6b28 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x208>
1400b6ac8:     	testq	%rcx, %rcx
1400b6acb:     	je	0x1400b6af0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x1d0>
1400b6acd:     	movzbl	(%rcx,%r9), %r10d
1400b6ad2:     	movb	%r10b, 0x38(%rsp,%r9)
1400b6ad7:     	movq	(%rdx,%r9,8), %r11
1400b6adb:     	testq	%r11, %r11
1400b6ade:     	jne	0x1400b6b0e <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x1ee>
1400b6ae0:     	jmp	0x1400b6ac0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x1a0>
1400b6ae2:     	nopw	%cs:(%rax,%rax)
1400b6af0:     	cmpq	$0x0, (%rdx,%r9,8)
1400b6af5:     	sete	%r10b
1400b6af9:     	addb	%r10b, %r10b
1400b6afc:     	orb	$0x61, %r10b
1400b6b00:     	movb	%r10b, 0x38(%rsp,%r9)
1400b6b05:     	movq	(%rdx,%r9,8), %r11
1400b6b09:     	testq	%r11, %r11
1400b6b0c:     	je	0x1400b6ac0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x1a0>
1400b6b0e:     	cmpb	$0x63, %r10b
1400b6b12:     	jne	0x1400b6bce <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x2ae>
1400b6b18:     	testq	%r8, %r8
1400b6b1b:     	je	0x1400b6ac0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x1a0>
1400b6b1d:     	cmpq	%r11, (%r8,%r9,8)
1400b6b21:     	je	0x1400b6ac0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x1a0>
1400b6b23:     	jmp	0x1400b6bce <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x2ae>
1400b6b28:     	movq	%r15, 0x48(%rsp)
1400b6b2d:     	movl	0x20(%rsp), %eax
1400b6b31:     	movl	%eax, %ebp
1400b6b33:     	testl	%eax, %eax
1400b6b35:     	jle	0x1400b6c35 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x315>
1400b6b3b:     	xorl	%r14d, %r14d
1400b6b3e:     	movl	$0x9, %r13d
1400b6b44:     	leaq	0x38(%rsp), %r15
1400b6b49:     	jmp	0x1400b6b5c <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x23c>
1400b6b4b:     	nopl	(%rax,%rax)
1400b6b50:     	incq	%r14
1400b6b53:     	cmpq	%r14, %rbp
1400b6b56:     	je	0x1400b6c35 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x315>
1400b6b5c:     	movq	0x28(%rsp), %rax
1400b6b61:     	movq	(%rax), %rax
1400b6b64:     	movq	(%rax,%r14,8), %r12
1400b6b68:     	movq	%r12, %rcx
1400b6b6b:     	movq	%rsi, %rdx
1400b6b6e:     	callq	0x140081350 <easyknack::BakedGraphProcessor::resolveChannels(easyknack::ProcessContext const&)>
1400b6b73:     	movq	0x8(%rsi), %r8
1400b6b77:     	cmpq	$0x9, %r8
1400b6b7b:     	cmovaeq	%r13, %r8
1400b6b7f:     	movq	%r12, %rcx
1400b6b82:     	movq	%r15, %rdx
1400b6b85:     	callq	0x1400b5c80 <easyknack::MultipleGraphNode::Instance::resolveAutoRate(char const*, unsigned long long)>
1400b6b8a:     	movq	%r12, %rcx
1400b6b8d:     	callq	0x1400b5710 <easyknack::MultipleGraphNode::Instance::reductionReady() const>
1400b6b92:     	testb	%al, %al
1400b6b94:     	je	0x1400b6bce <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x2ae>
1400b6b96:     	movq	0x38(%r12), %rax
1400b6b9b:     	movslq	0x5c(%r12), %rcx
1400b6ba0:     	movq	(%rax), %rax
1400b6ba3:     	imulq	$0x1378, %rcx, %rcx     # imm = 0x1378
1400b6baa:     	movq	0x68(%rax,%rcx), %rax
1400b6baf:     	movq	0x8(%rax), %rcx
1400b6bb3:     	cmpl	$0x0, (%rcx)
1400b6bb6:     	jns	0x1400b6b50 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x230>
1400b6bb8:     	movsd	0x48(%rax), %xmm0
1400b6bbd:     	movss	0x40(%rax), %xmm1
1400b6bc2:     	cvtss2sd	%xmm1, %xmm1
1400b6bc6:     	ucomisd	%xmm1, %xmm0
1400b6bca:     	jne	0x1400b6bce <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x2ae>
1400b6bcc:     	jnp	0x1400b6b50 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x230>
1400b6bce:     	xorl	%ebp, %ebp
1400b6bd0:     	movl	%ebp, %eax
1400b6bd2:     	movaps	0x50(%rsp), %xmm6
1400b6bd7:     	movaps	0x60(%rsp), %xmm7
1400b6bdc:     	movaps	0x70(%rsp), %xmm8
1400b6be2:     	movaps	0x80(%rsp), %xmm9
1400b6beb:     	movaps	0x90(%rsp), %xmm10
1400b6bf4:     	movaps	0xa0(%rsp), %xmm11
1400b6bfd:     	movaps	0xb0(%rsp), %xmm12
1400b6c06:     	movaps	0xc0(%rsp), %xmm13
1400b6c0f:     	movaps	0xd0(%rsp), %xmm14
1400b6c18:     	movaps	0xe0(%rsp), %xmm15
1400b6c21:     	addq	$0xf8, %rsp
1400b6c28:     	popq	%rbx
1400b6c29:     	popq	%rbp
1400b6c2a:     	popq	%rdi
1400b6c2b:     	popq	%rsi
1400b6c2c:     	popq	%r12
1400b6c2e:     	popq	%r13
1400b6c30:     	popq	%r14
1400b6c32:     	popq	%r15
1400b6c34:     	retq
1400b6c35:     	movq	0x28(%rsp), %rax
1400b6c3a:     	cmpb	$0x0, 0xc0(%rax)
1400b6c41:     	movq	0x48(%rsp), %r12
1400b6c46:     	jne	0x1400b6dc1 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x4a1>
1400b6c4c:     	movslq	0x48(%r12), %rax
1400b6c51:     	testq	%rax, %rax
1400b6c54:     	jle	0x1400b6db5 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x495>
1400b6c5a:     	movq	0x28(%rsp), %rcx
1400b6c5f:     	movq	0x18(%rcx), %rcx
1400b6c63:     	movslq	0x20(%rsp), %rdx
1400b6c68:     	cmpl	$0x8, %eax
1400b6c6b:     	jae	0x1400b6c75 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x355>
1400b6c6d:     	xorl	%r8d, %r8d
1400b6c70:     	jmp	0x1400b6e45 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x525>
1400b6c75:     	movl	%eax, %r8d
1400b6c78:     	andl	$0x7ffffff8, %r8d       # imm = 0x7FFFFFF8
1400b6c7f:     	movq	%rdx, %xmm0
1400b6c84:     	pshufd	$0x44, %xmm0, %xmm0     # xmm0 = xmm0[0,1,0,1]
1400b6c89:     	movl	%eax, %r9d
1400b6c8c:     	shrl	$0x3, %r9d
1400b6c90:     	andl	$0xfffffff, %r9d        # imm = 0xFFFFFFF
1400b6c97:     	shlq	$0x5, %r9
1400b6c9b:     	movdqa	0xc1a0d(%rip), %xmm1    # 0x1401786b0 <.refptr._newmode+0x5628>
1400b6ca3:     	movdqa	0xc1a15(%rip), %xmm2    # 0x1401786c0 <.refptr._newmode+0x5638>
1400b6cab:     	xorl	%r10d, %r10d
1400b6cae:     	movdqa	0xc1a1a(%rip), %xmm3    # 0x1401786d0 <.refptr._newmode+0x5648>
1400b6cb6:     	movdqa	0xc1a22(%rip), %xmm4    # 0x1401786e0 <.refptr._newmode+0x5658>
1400b6cbe:     	pxor	%xmm4, %xmm0
1400b6cc2:     	movaps	0xc1a27(%rip), %xmm5    # 0x1401786f0 <.refptr._newmode+0x5668>
1400b6cc9:     	movdqa	0xc1a2f(%rip), %xmm6    # 0x140178700 <.refptr._newmode+0x5678>
1400b6cd1:     	nopw	%cs:(%rax,%rax)
1400b6ce0:     	movdqa	%xmm1, %xmm9
1400b6ce5:     	paddq	%xmm3, %xmm9
1400b6cea:     	movdqa	%xmm2, %xmm8
1400b6cef:     	paddq	%xmm3, %xmm8
1400b6cf4:     	movdqa	%xmm2, %xmm10
1400b6cf9:     	pxor	%xmm4, %xmm10
1400b6cfe:     	movdqa	%xmm0, %xmm7
1400b6d02:     	pcmpgtd	%xmm10, %xmm7
1400b6d07:     	movdqa	%xmm1, %xmm11
1400b6d0c:     	pxor	%xmm4, %xmm11
1400b6d11:     	movdqa	%xmm0, %xmm12
1400b6d16:     	pcmpgtd	%xmm11, %xmm12
1400b6d1b:     	movdqa	%xmm7, %xmm13
1400b6d20:     	shufps	$0x88, %xmm12, %xmm13   # xmm13 = xmm13[0,2],xmm12[0,2]
1400b6d25:     	pcmpeqd	%xmm0, %xmm10
1400b6d2a:     	pcmpeqd	%xmm0, %xmm11
1400b6d2f:     	shufps	$0xdd, %xmm11, %xmm10   # xmm10 = xmm10[1,3],xmm11[1,3]
1400b6d34:     	andps	%xmm13, %xmm10
1400b6d38:     	shufps	$0xdd, %xmm12, %xmm7    # xmm7 = xmm7[1,3],xmm12[1,3]
1400b6d3d:     	orps	%xmm10, %xmm7
1400b6d41:     	pxor	%xmm4, %xmm8
1400b6d46:     	movdqa	%xmm0, %xmm10
1400b6d4b:     	pcmpgtd	%xmm8, %xmm10
1400b6d50:     	pxor	%xmm4, %xmm9
1400b6d55:     	movdqa	%xmm0, %xmm11
1400b6d5a:     	pcmpgtd	%xmm9, %xmm11
1400b6d5f:     	movdqa	%xmm10, %xmm12
1400b6d64:     	shufps	$0x88, %xmm11, %xmm12   # xmm12 = xmm12[0,2],xmm11[0,2]
1400b6d69:     	pcmpeqd	%xmm0, %xmm8
1400b6d6e:     	pcmpeqd	%xmm0, %xmm9
1400b6d73:     	shufps	$0xdd, %xmm9, %xmm8     # xmm8 = xmm8[1,3],xmm9[1,3]
1400b6d78:     	andps	%xmm12, %xmm8
1400b6d7c:     	shufps	$0xdd, %xmm11, %xmm10   # xmm10 = xmm10[1,3],xmm11[1,3]
1400b6d81:     	orps	%xmm8, %xmm10
1400b6d85:     	andps	%xmm5, %xmm7
1400b6d88:     	andps	%xmm5, %xmm10
1400b6d8c:     	movups	%xmm7, (%rcx,%r10)
1400b6d91:     	movups	%xmm10, 0x10(%rcx,%r10)
1400b6d97:     	paddq	%xmm6, %xmm2
1400b6d9b:     	paddq	%xmm6, %xmm1
1400b6d9f:     	addq	$0x20, %r10
1400b6da3:     	cmpq	%r10, %r9
1400b6da6:     	jne	0x1400b6ce0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x3c0>
1400b6dac:     	cmpq	%rax, %r8
1400b6daf:     	jne	0x1400b6e45 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x525>
1400b6db5:     	movq	0x28(%rsp), %rax
1400b6dba:     	movb	$0x1, 0xc0(%rax)
1400b6dc1:     	xorps	%xmm0, %xmm0
1400b6dc4:     	movss	%xmm0, 0x34(%rsp)
1400b6dca:     	cmpl	$0x0, 0x20(%rsp)
1400b6dcf:     	je	0x1400b6e20 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x500>
1400b6dd1:     	movq	0x28(%rsp), %rax
1400b6dd6:     	movq	(%rax), %rax
1400b6dd9:     	movq	(%rax), %rax
1400b6ddc:     	movq	0x38(%rax), %rax
1400b6de0:     	movq	0x4c8(%rax), %rcx
1400b6de7:     	movslq	(%rcx), %rcx
1400b6dea:     	movq	(%rax), %rax
1400b6ded:     	imulq	$0x1378, %rcx, %rcx     # imm = 0x1378
1400b6df4:     	movq	0x68(%rax,%rcx), %rax
1400b6df9:     	movzbl	0x40(%rax), %ecx
1400b6dfd:     	testb	$0x1, %cl
1400b6e00:     	je	0x1400b6e2d <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x50d>
1400b6e02:     	movq	0x48(%rax), %rdx
1400b6e06:     	cmpq	$0x8, %rdx
1400b6e0a:     	je	0x1400b6e3a <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x51a>
1400b6e0c:     	movl	$0x0, 0x24(%rsp)
1400b6e14:     	cmpl	$0x0, 0x20(%rsp)
1400b6e19:     	jg	0x1400b6e94 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x574>
1400b6e1b:     	jmp	0x1400b723a <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x91a>
1400b6e20:     	movl	$0x0, 0x24(%rsp)
1400b6e28:     	jmp	0x1400b723a <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x91a>
1400b6e2d:     	movl	%ecx, %edx
1400b6e2f:     	shrb	%dl
1400b6e31:     	movzbl	%dl, %edx
1400b6e34:     	cmpq	$0x8, %rdx
1400b6e38:     	jne	0x1400b6e0c <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x4ec>
1400b6e3a:     	testb	$0x1, %cl
1400b6e3d:     	je	0x1400b6e71 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x551>
1400b6e3f:     	movq	0x50(%rax), %rax
1400b6e43:     	jmp	0x1400b6e75 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x555>
1400b6e45:     	movd	0xc1853(%rip), %xmm0    # 0x1401786a0 <.refptr._newmode+0x5618>
1400b6e4d:     	jmp	0x1400b6e62 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x542>
1400b6e4f:     	nop
1400b6e50:     	movd	%xmm1, (%rcx,%r8,4)
1400b6e56:     	incq	%r8
1400b6e59:     	cmpq	%r8, %rax
1400b6e5c:     	je	0x1400b6db5 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x495>
1400b6e62:     	movdqa	%xmm0, %xmm1
1400b6e66:     	cmpq	%rdx, %r8
1400b6e69:     	jl	0x1400b6e50 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x530>
1400b6e6b:     	pxor	%xmm1, %xmm1
1400b6e6f:     	jmp	0x1400b6e50 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x530>
1400b6e71:     	addq	$0x41, %rax
1400b6e75:     	movabsq	$0x6465746867696577, %rcx # imm = 0x6465746867696577
1400b6e7f:     	cmpq	%rcx, (%rax)
1400b6e82:     	sete	%al
1400b6e85:     	movl	%eax, 0x24(%rsp)
1400b6e89:     	cmpl	$0x0, 0x20(%rsp)
1400b6e8e:     	jle	0x1400b723a <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x91a>
1400b6e94:     	movss	%xmm0, 0x34(%rsp)
1400b6e9a:     	xorl	%r14d, %r14d
1400b6e9d:     	movd	0xc17fb(%rip), %xmm6    # 0x1401786a0 <.refptr._newmode+0x5618>
1400b6ea5:     	movabsq	$0x7fffffffffffffff, %r13 # imm = 0x7FFFFFFFFFFFFFFF
1400b6eaf:     	movsd	0xc1858(%rip), %xmm8    # 0x140178710 <.refptr._newmode+0x5688>
1400b6eb8:     	movsd	0xc1857(%rip), %xmm9    # 0x140178718 <.refptr._newmode+0x5690>
1400b6ec1:     	movsd	0xc1856(%rip), %xmm10   # 0x140178720 <.refptr._newmode+0x5698>
1400b6eca:     	movsd	0xc1855(%rip), %xmm11   # 0x140178728 <.refptr._newmode+0x56a0>
1400b6ed3:     	movsd	0xc1854(%rip), %xmm12   # 0x140178730 <.refptr._newmode+0x56a8>
1400b6edc:     	movsd	0xc1853(%rip), %xmm13   # 0x140178738 <.refptr._newmode+0x56b0>
1400b6ee5:     	movsd	0xc1852(%rip), %xmm14   # 0x140178740 <.refptr._newmode+0x56b8>
1400b6eee:     	movsd	0xc1852(%rip), %xmm7    # 0x140178748 <.refptr._newmode+0x56c0>
1400b6ef6:     	movsd	0xc1861(%rip), %xmm15   # 0x140178760 <.refptr._newmode+0x56d8>
1400b6eff:     	jmp	0x1400b6f71 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x651>
1400b6f01:     	nopw	%cs:(%rax,%rax)
1400b6f10:     	xorl	%edx, %edx
1400b6f12:     	movss	0x34(%rsp), %xmm2
1400b6f18:     	addss	%xmm0, %xmm2
1400b6f1c:     	movss	%xmm2, 0x34(%rsp)
1400b6f22:     	movsd	0x38(%rax), %xmm0
1400b6f27:     	divsd	%xmm15, %xmm0
1400b6f2c:     	movsd	%xmm0, (%rbx,%r14,8)
1400b6f32:     	movsd	%xmm1, 0x800(%rbx,%r14,8)
1400b6f3c:     	imulq	$0x14028, %rcx, %rcx    # imm = 0x14028
1400b6f43:     	addq	0x58(%rax), %rcx
1400b6f47:     	movl	%edx, %r8d
1400b6f4a:     	shll	$0xd, %r8d
1400b6f4e:     	leal	(%r8,%rdx,4), %edx
1400b6f52:     	addq	%rcx, %rdx
1400b6f55:     	movq	%rdx, 0x1400(%rbx,%r14,8)
1400b6f5d:     	movq	%rax, 0x1c00(%rbx,%r14,8)
1400b6f65:     	incq	%r14
1400b6f68:     	cmpq	%rbp, %r14
1400b6f6b:     	je	0x1400b723a <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x91a>
1400b6f71:     	movq	0x28(%rsp), %rax
1400b6f76:     	movq	(%rax), %rax
1400b6f79:     	movq	(%rax,%r14,8), %r15
1400b6f7d:     	movq	0x78(%r15), %rax
1400b6f81:     	movq	0x80(%r15), %rcx
1400b6f88:     	cmpq	%rcx, %rax
1400b6f8b:     	je	0x1400b6fa0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x680>
1400b6f8d:     	nopl	(%rax)
1400b6f90:     	movq	(%rax), %rdx
1400b6f93:     	movq	%rbp, 0x40(%rdx)
1400b6f97:     	addq	$0x8, %rax
1400b6f9b:     	cmpq	%rcx, %rax
1400b6f9e:     	jne	0x1400b6f90 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x670>
1400b6fa0:     	movq	0x38(%r15), %rdx
1400b6fa4:     	movq	0x4b0(%rdx), %rax
1400b6fab:     	movq	0x4b8(%rdx), %rcx
1400b6fb2:     	cmpq	%rcx, %rax
1400b6fb5:     	je	0x1400b6fe5 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x6c5>
1400b6fb7:     	movq	(%rdx), %rdx
1400b6fba:     	nopw	(%rax,%rax)
1400b6fc0:     	movslq	(%rax), %r8
1400b6fc3:     	imulq	$0x1378, %r8, %r8       # imm = 0x1378
1400b6fca:     	movq	0x68(%rdx,%r8), %r9
1400b6fcf:     	movq	%rsi, 0x38(%r9)
1400b6fd3:     	movq	0x70(%rdx,%r8), %r8
1400b6fd8:     	movq	%rsi, 0x38(%r8)
1400b6fdc:     	addq	$0x4, %rax
1400b6fe0:     	cmpq	%rcx, %rax
1400b6fe3:     	jne	0x1400b6fc0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x6a0>
1400b6fe5:     	movq	%r15, %rcx
1400b6fe8:     	movl	%edi, %edx
1400b6fea:     	callq	0x1400b93d0 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)>
1400b6fef:     	movq	0x38(%r15), %rdx
1400b6ff3:     	movslq	0x5c(%r15), %rax
1400b6ff7:     	movq	(%rdx), %rcx
1400b6ffa:     	imulq	$0x1378, %rax, %rax     # imm = 0x1378
1400b7001:     	movq	0x68(%rcx,%rax), %rax
1400b7006:     	movq	0x4c8(%rdx), %rdx
1400b700d:     	movslq	(%rdx), %rdx
1400b7010:     	imulq	$0x1378, %rdx, %rdx     # imm = 0x1378
1400b7017:     	movq	0x68(%rcx,%rdx), %r8
1400b701c:     	movq	0x8(%r8), %rdx
1400b7020:     	movl	0x4(%rdx), %edx
1400b7023:     	testl	%edx, %edx
1400b7025:     	js	0x1400b7050 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x730>
1400b7027:     	movzbl	%dl, %r8d
1400b702b:     	imulq	$0x1378, %r8, %r8       # imm = 0x1378
1400b7032:     	movq	0x1348(%rcx,%r8), %r8
1400b703a:     	movq	(%r8), %r8
1400b703d:     	shrl	$0x4, %edx
1400b7040:     	andl	$-0x10, %edx
1400b7043:     	movq	(%r8,%rdx), %r8
1400b7047:     	jmp	0x1400b7054 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x734>
1400b7049:     	nopl	(%rax)
1400b7050:     	addq	$0x3c, %r8
1400b7054:     	movd	(%r8), %xmm0
1400b7059:     	movd	%xmm0, %edx
1400b705d:     	xorps	%xmm1, %xmm1
1400b7060:     	testl	%edx, %edx
1400b7062:     	js	0x1400b7068 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x748>
1400b7064:     	movdqa	%xmm0, %xmm1
1400b7068:     	movl	%edx, %r8d
1400b706b:     	andl	$0x7fffffff, %r8d       # imm = 0x7FFFFFFF
1400b7072:     	addl	$0xff800000, %r8d       # imm = 0xFF800000
1400b7079:     	cmpl	$0x7f000000, %r8d       # imm = 0x7F000000
1400b7080:     	jae	0x1400b70d0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x7b0>
1400b7082:     	decl	%edx
1400b7084:     	cmpl	$0x7fffff, %edx         # imm = 0x7FFFFF
1400b708a:     	jae	0x1400b70dd <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x7bd>
1400b708c:     	movd	%xmm0, 0x1000(%rbx,%r14,4)
1400b7096:     	cmpb	$0x0, 0x24(%rsp)
1400b709b:     	je	0x1400b70f1 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x7d1>
1400b709d:     	movq	0x8(%rax), %rdx
1400b70a1:     	movl	(%rdx), %edx
1400b70a3:     	testl	%edx, %edx
1400b70a5:     	js	0x1400b70ff <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x7df>
1400b70a7:     	movzbl	%dl, %r8d
1400b70ab:     	imulq	$0x1378, %r8, %r8       # imm = 0x1378
1400b70b2:     	movq	0x1348(%rcx,%r8), %rcx
1400b70ba:     	movq	(%rcx), %rcx
1400b70bd:     	shrl	$0x4, %edx
1400b70c0:     	andl	$-0x10, %edx
1400b70c3:     	movq	(%rcx,%rdx), %rcx
1400b70c7:     	jmp	0x1400b7103 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x7e3>
1400b70c9:     	nopl	(%rax)
1400b70d0:     	xorps	%xmm1, %xmm1
1400b70d3:     	decl	%edx
1400b70d5:     	cmpl	$0x7fffff, %edx         # imm = 0x7FFFFF
1400b70db:     	jb	0x1400b708c <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x76c>
1400b70dd:     	movaps	%xmm1, %xmm0
1400b70e0:     	movss	%xmm0, 0x1000(%rbx,%r14,4)
1400b70ea:     	cmpb	$0x0, 0x24(%rsp)
1400b70ef:     	jne	0x1400b709d <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x77d>
1400b70f1:     	movdqa	%xmm6, %xmm0
1400b70f5:     	movq	0x8(%rax), %rdx
1400b70f9:     	movl	(%rdx), %edx
1400b70fb:     	testl	%edx, %edx
1400b70fd:     	jns	0x1400b70a7 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x787>
1400b70ff:     	leaq	0x40(%rax), %rcx
1400b7103:     	movss	(%rcx), %xmm1
1400b7107:     	cvtss2sd	%xmm1, %xmm1
1400b710b:     	movq	%xmm1, %rcx
1400b7110:     	andq	%r13, %rcx
1400b7113:     	xorps	%xmm2, %xmm2
1400b7116:     	movabsq	$0x7ff0000000000000, %rdx # imm = 0x7FF0000000000000
1400b7120:     	cmpq	%rdx, %rcx
1400b7123:     	jl	0x1400b7128 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x808>
1400b7125:     	xorps	%xmm1, %xmm1
1400b7128:     	movapd	%xmm8, %xmm3
1400b712d:     	minsd	%xmm1, %xmm3
1400b7131:     	cmpltsd	%xmm2, %xmm1
1400b7136:     	andnpd	%xmm3, %xmm1
1400b713a:     	divsd	0x20(%rsi), %xmm1
1400b713f:     	movl	0x50(%rax), %ecx
1400b7142:     	testq	%rcx, %rcx
1400b7145:     	je	0x1400b6f10 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x5f0>
1400b714b:     	movapd	%xmm1, %xmm2
1400b714f:     	addsd	%xmm1, %xmm2
1400b7153:     	ucomisd	%xmm2, %xmm9
1400b7158:     	movl	$0x0, %edx
1400b715d:     	jbe	0x1400b6f12 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x5f2>
1400b7163:     	movapd	%xmm1, %xmm2
1400b7167:     	mulsd	%xmm10, %xmm2
1400b716c:     	movl	$0x1, %edx
1400b7171:     	ucomisd	%xmm2, %xmm9
1400b7176:     	jbe	0x1400b6f12 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x5f2>
1400b717c:     	movapd	%xmm1, %xmm2
1400b7180:     	mulsd	%xmm11, %xmm2
1400b7185:     	movl	$0x2, %edx
1400b718a:     	ucomisd	%xmm2, %xmm9
1400b718f:     	jbe	0x1400b6f12 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x5f2>
1400b7195:     	movapd	%xmm1, %xmm2
1400b7199:     	mulsd	%xmm12, %xmm2
1400b719e:     	movl	$0x3, %edx
1400b71a3:     	ucomisd	%xmm2, %xmm9
1400b71a8:     	jbe	0x1400b6f12 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x5f2>
1400b71ae:     	movapd	%xmm1, %xmm2
1400b71b2:     	mulsd	%xmm13, %xmm2
1400b71b7:     	movl	$0x4, %edx
1400b71bc:     	ucomisd	%xmm2, %xmm9
1400b71c1:     	jbe	0x1400b6f12 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x5f2>
1400b71c7:     	movapd	%xmm1, %xmm2
1400b71cb:     	mulsd	%xmm14, %xmm2
1400b71d0:     	movl	$0x5, %edx
1400b71d5:     	ucomisd	%xmm2, %xmm9
1400b71da:     	jbe	0x1400b6f12 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x5f2>
1400b71e0:     	movapd	%xmm1, %xmm2
1400b71e4:     	mulsd	%xmm7, %xmm2
1400b71e8:     	movl	$0x6, %edx
1400b71ed:     	ucomisd	%xmm2, %xmm9
1400b71f2:     	jbe	0x1400b6f12 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x5f2>
1400b71f8:     	movapd	%xmm1, %xmm2
1400b71fc:     	mulsd	0xc154c(%rip), %xmm2    # 0x140178750 <.refptr._newmode+0x56c8>
1400b7204:     	movl	$0x7, %edx
1400b7209:     	ucomisd	%xmm2, %xmm9
1400b720e:     	jbe	0x1400b6f12 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x5f2>
1400b7214:     	movapd	%xmm1, %xmm2
1400b7218:     	mulsd	0xc1538(%rip), %xmm2    # 0x140178758 <.refptr._newmode+0x56d0>
1400b7220:     	movl	$0x8, %edx
1400b7225:     	ucomisd	%xmm2, %xmm9
1400b722a:     	jbe	0x1400b6f12 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x5f2>
1400b7230:     	movl	$0x9, %edx
1400b7235:     	jmp	0x1400b6f12 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x5f2>
1400b723a:     	cmpb	$0x0, 0x52(%r12)
1400b7240:     	je	0x1400b7289 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x969>
1400b7242:     	movq	0x28(%rsp), %rax
1400b7247:     	movq	(%rax), %rax
1400b724a:     	movq	(%rax), %rax
1400b724d:     	movq	0x38(%rax), %rax
1400b7251:     	movq	0x4c8(%rax), %rcx
1400b7258:     	movslq	(%rcx), %rcx
1400b725b:     	movq	(%rax), %rax
1400b725e:     	imulq	$0x1378, %rcx, %rcx     # imm = 0x1378
1400b7265:     	movq	0x68(%rax,%rcx), %rax
1400b726a:     	movzbl	0x40(%rax), %ecx
1400b726e:     	testb	$0x1, %cl
1400b7271:     	je	0x1400b7299 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x979>
1400b7273:     	movq	0x48(%rax), %rdx
1400b7277:     	cmpq	$0x4, %rdx
1400b727b:     	je	0x1400b72a6 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x986>
1400b727d:     	xorl	%eax, %eax
1400b727f:     	testq	%rdi, %rdi
1400b7282:     	jne	0x1400b72c7 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x9a7>
1400b7284:     	jmp	0x1400b74ac <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xb8c>
1400b7289:     	movzbl	0x50(%r12), %eax
1400b728f:     	testq	%rdi, %rdi
1400b7292:     	jne	0x1400b72c7 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x9a7>
1400b7294:     	jmp	0x1400b74ac <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xb8c>
1400b7299:     	movl	%ecx, %edx
1400b729b:     	shrb	%dl
1400b729d:     	movzbl	%dl, %edx
1400b72a0:     	cmpq	$0x4, %rdx
1400b72a4:     	jne	0x1400b727d <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x95d>
1400b72a6:     	testb	$0x1, %cl
1400b72a9:     	je	0x1400b72b1 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x991>
1400b72ab:     	movq	0x50(%rax), %rax
1400b72af:     	jmp	0x1400b72b5 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x995>
1400b72b1:     	addq	$0x41, %rax
1400b72b5:     	cmpl	$0x6e61656d, (%rax)     # imm = 0x6E61656D
1400b72bb:     	sete	%al
1400b72be:     	testq	%rdi, %rdi
1400b72c1:     	je	0x1400b74ac <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xb8c>
1400b72c7:     	movq	0x28(%rsp), %rcx
1400b72cc:     	movq	0x30(%rcx), %rcx
1400b72d0:     	movq	(%rcx), %r14
1400b72d3:     	movl	0x24(%rsp), %ecx
1400b72d7:     	orb	%al, %cl
1400b72d9:     	movl	%ecx, 0x24(%rsp)
1400b72dd:     	xorl	%r15d, %r15d
1400b72e0:     	xorps	%xmm8, %xmm8
1400b72e4:     	movapd	0xc1493(%rip), %xmm11   # 0x140178780 <.refptr._newmode+0x56f8>
1400b72ed:     	movapd	0xc149a(%rip), %xmm12   # 0x140178790 <.refptr._newmode+0x5708>
1400b72f6:     	movapd	0xc14a1(%rip), %xmm13   # 0x1401787a0 <.refptr._newmode+0x5718>
1400b72ff:     	movapd	0xc14a8(%rip), %xmm14   # 0x1401787b0 <.refptr._newmode+0x5728>
1400b7308:     	movq	0xc14af(%rip), %xmm15   # 0x1401787c0 <.refptr._newmode+0x5738>
1400b7311:     	movapd	0xc14b7(%rip), %xmm7    # 0x1401787d0 <.refptr._newmode+0x5748>
1400b7319:     	jmp	0x1400b7332 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xa12>
1400b731b:     	nopl	(%rax,%rax)
1400b7320:     	movss	%xmm0, (%r14,%r15,4)
1400b7326:     	incq	%r15
1400b7329:     	cmpq	%rdi, %r15
1400b732c:     	je	0x1400b74ac <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xb8c>
1400b7332:     	xorps	%xmm9, %xmm9
1400b7336:     	cmpl	$0x0, 0x20(%rsp)
1400b733b:     	jle	0x1400b7480 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xb60>
1400b7341:     	xorl	%r13d, %r13d
1400b7344:     	jmp	0x1400b745b <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xb3b>
1400b7349:     	nopl	(%rax)
1400b7350:     	movq	0x1400(%rbx,%r13,8), %r12
1400b7358:     	movsd	(%rbx,%r13,8), %xmm6
1400b735e:     	movapd	%xmm6, %xmm0
1400b7362:     	callq	0x140171a40 <floor>
1400b7367:     	movapd	%xmm6, %xmm1
1400b736b:     	subsd	%xmm0, %xmm1
1400b736f:     	mulsd	0xc13f1(%rip), %xmm1    # 0x140178768 <.refptr._newmode+0x56e0>
1400b7377:     	cvttsd2si	%xmm1, %rcx
1400b737c:     	movq	%rcx, %rdx
1400b737f:     	sarq	$0x3f, %rdx
1400b7383:     	movapd	%xmm1, %xmm0
1400b7387:     	subsd	0xc13e1(%rip), %xmm0    # 0x140178770 <.refptr._newmode+0x56e8>
1400b738f:     	cvttsd2si	%xmm0, %rax
1400b7394:     	andq	%rdx, %rax
1400b7397:     	orq	%rcx, %rax
1400b739a:     	leal	0x1(%rax), %ecx
1400b739d:     	andl	$0x7ff, %ecx            # imm = 0x7FF
1400b73a3:     	movss	(%r12,%rax,4), %xmm0
1400b73a9:     	movss	(%r12,%rcx,4), %xmm2
1400b73af:     	unpcklps	%xmm0, %xmm2            # xmm2 = xmm2[0],xmm0[0],xmm2[1],xmm0[1]
1400b73b2:     	cvtps2pd	%xmm2, %xmm2
1400b73b5:     	movapd	%xmm2, %xmm0
1400b73b9:     	andpd	%xmm11, %xmm0
1400b73be:     	movapd	%xmm0, %xmm3
1400b73c2:     	cmpneqpd	%xmm12, %xmm3
1400b73c8:     	cmpordpd	%xmm12, %xmm0
1400b73ce:     	andpd	%xmm3, %xmm0
1400b73d2:     	andpd	%xmm2, %xmm0
1400b73d6:     	movapd	%xmm14, %xmm2
1400b73db:     	minpd	%xmm0, %xmm2
1400b73df:     	cmpltpd	%xmm13, %xmm0
1400b73e5:     	movapd	%xmm13, %xmm3
1400b73ea:     	andpd	%xmm0, %xmm3
1400b73ee:     	andnpd	%xmm2, %xmm0
1400b73f2:     	orpd	%xmm3, %xmm0
1400b73f6:     	pshufd	$0xee, %xmm0, %xmm2     # xmm2 = xmm0[2,3,2,3]
1400b73fb:     	subsd	%xmm2, %xmm0
1400b73ff:     	movq	%rax, %xmm3
1400b7404:     	punpckldq	%xmm15, %xmm3   # xmm3 = xmm3[0],xmm15[0],xmm3[1],xmm15[1]
1400b7409:     	subpd	%xmm7, %xmm3
1400b740d:     	movapd	%xmm3, %xmm4
1400b7411:     	unpckhpd	%xmm3, %xmm4            # xmm4 = xmm4[1],xmm3[1]
1400b7415:     	addsd	%xmm3, %xmm4
1400b7419:     	subsd	%xmm4, %xmm1
1400b741d:     	mulsd	%xmm0, %xmm1
1400b7421:     	addsd	%xmm2, %xmm1
1400b7425:     	xorps	%xmm0, %xmm0
1400b7428:     	cvtsd2ss	%xmm1, %xmm0
1400b742c:     	mulss	%xmm0, %xmm10
1400b7431:     	addss	%xmm10, %xmm9
1400b7436:     	addsd	0x800(%rbx,%r13,8), %xmm6
1400b7440:     	movapd	%xmm6, %xmm0
1400b7444:     	callq	0x140171a40 <floor>
1400b7449:     	subsd	%xmm0, %xmm6
1400b744d:     	movsd	%xmm6, (%rbx,%r13,8)
1400b7453:     	incq	%r13
1400b7456:     	cmpq	%r13, %rbp
1400b7459:     	je	0x1400b7480 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xb60>
1400b745b:     	movss	0x1000(%rbx,%r13,4), %xmm10
1400b7465:     	ucomiss	%xmm8, %xmm10
1400b7469:     	jne	0x1400b7350 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xa30>
1400b746f:     	jp	0x1400b7350 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xa30>
1400b7475:     	movsd	(%rbx,%r13,8), %xmm6
1400b747b:     	jmp	0x1400b7436 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xb16>
1400b747d:     	nopl	(%rax)
1400b7480:     	xorps	%xmm0, %xmm0
1400b7483:     	movss	0x34(%rsp), %xmm1
1400b7489:     	ucomiss	%xmm0, %xmm1
1400b748c:     	jbe	0x1400b7498 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xb78>
1400b748e:     	movaps	%xmm9, %xmm0
1400b7492:     	divss	0x34(%rsp), %xmm0
1400b7498:     	testb	$0x1, 0x24(%rsp)
1400b749d:     	jne	0x1400b7320 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xa00>
1400b74a3:     	movaps	%xmm9, %xmm0
1400b74a7:     	jmp	0x1400b7320 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xa00>
1400b74ac:     	cmpl	$0x0, 0x20(%rsp)
1400b74b1:     	jle	0x1400b75ca <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xcaa>
1400b74b7:     	leaq	-0x1(%rdi), %rax
1400b74bb:     	movl	%edi, %ecx
1400b74bd:     	andl	$0x3, %ecx
1400b74c0:     	movq	%rdi, %rdx
1400b74c3:     	andq	$-0x4, %rdx
1400b74c7:     	xorl	%r8d, %r8d
1400b74ca:     	movsd	0xc128e(%rip), %xmm0    # 0x140178760 <.refptr._newmode+0x56d8>
1400b74d2:     	jmp	0x1400b74f2 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xbd2>
1400b74d4:     	nopw	%cs:(%rax,%rax)
1400b74e0:     	movsd	%xmm2, 0x48(%r9)
1400b74e6:     	incq	%r8
1400b74e9:     	cmpq	%rbp, %r8
1400b74ec:     	je	0x1400b75ca <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xcaa>
1400b74f2:     	testq	%rdi, %rdi
1400b74f5:     	sete	%r10b
1400b74f9:     	movq	0x1c00(%rbx,%r8,8), %r9
1400b7501:     	movsd	(%rbx,%r8,8), %xmm1
1400b7507:     	mulsd	%xmm0, %xmm1
1400b750b:     	movsd	%xmm1, 0x38(%r9)
1400b7511:     	movsd	0x48(%r9), %xmm2
1400b7517:     	movss	0x40(%r9), %xmm1
1400b751d:     	cvtss2sd	%xmm1, %xmm1
1400b7521:     	ucomisd	%xmm2, %xmm1
1400b7525:     	setnp	%r11b
1400b7529:     	sete	%r14b
1400b752d:     	andb	%r11b, %r14b
1400b7530:     	orb	%r10b, %r14b
1400b7533:     	jne	0x1400b74e6 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xbc6>
1400b7535:     	movsd	0x60(%r9), %xmm3
1400b753b:     	cmpq	$0x3, %rax
1400b753f:     	jb	0x1400b7596 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xc76>
1400b7541:     	movq	%rdx, %r10
1400b7544:     	nopw	%cs:(%rax,%rax)
1400b7550:     	movapd	%xmm1, %xmm4
1400b7554:     	subsd	%xmm2, %xmm4
1400b7558:     	mulsd	%xmm3, %xmm4
1400b755c:     	addsd	%xmm2, %xmm4
1400b7560:     	movapd	%xmm1, %xmm2
1400b7564:     	subsd	%xmm4, %xmm2
1400b7568:     	mulsd	%xmm3, %xmm2
1400b756c:     	addsd	%xmm4, %xmm2
1400b7570:     	movapd	%xmm1, %xmm4
1400b7574:     	subsd	%xmm2, %xmm4
1400b7578:     	mulsd	%xmm3, %xmm4
1400b757c:     	addsd	%xmm2, %xmm4
1400b7580:     	movapd	%xmm1, %xmm2
1400b7584:     	subsd	%xmm4, %xmm2
1400b7588:     	mulsd	%xmm3, %xmm2
1400b758c:     	addsd	%xmm4, %xmm2
1400b7590:     	addq	$-0x4, %r10
1400b7594:     	jne	0x1400b7550 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xc30>
1400b7596:     	testq	%rcx, %rcx
1400b7599:     	je	0x1400b74e0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xbc0>
1400b759f:     	movq	%rcx, %r10
1400b75a2:     	nopw	%cs:(%rax,%rax)
1400b75b0:     	movapd	%xmm1, %xmm4
1400b75b4:     	subsd	%xmm2, %xmm4
1400b75b8:     	mulsd	%xmm3, %xmm4
1400b75bc:     	addsd	%xmm4, %xmm2
1400b75c0:     	decq	%r10
1400b75c3:     	jne	0x1400b75b0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xc90>
1400b75c5:     	jmp	0x1400b74e0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xbc0>
1400b75ca:     	testq	%rdi, %rdi
1400b75cd:     	je	0x1400b75ee <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0xcce>
1400b75cf:     	shlq	$0x2, %rdi
1400b75d3:     	movq	0x28(%rsp), %rcx
1400b75d8:     	movq	0x30(%rcx), %rax
1400b75dc:     	movq	0x48(%rcx), %rcx
1400b75e0:     	movq	(%rcx), %rcx
1400b75e3:     	movq	(%rax), %rdx
1400b75e6:     	movq	%rdi, %r8
1400b75e9:     	callq	0x140171b50 <memmove>
1400b75ee:     	movq	0x10(%rsi), %rcx
1400b75f2:     	movb	$0x1, %bpl
1400b75f5:     	testq	%rcx, %rcx
1400b75f8:     	je	0x1400b6bd0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x2b0>
1400b75fe:     	movq	0x18(%rsi), %r8
1400b7602:     	testq	%r8, %r8
1400b7605:     	je	0x1400b6bd0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x2b0>
1400b760b:     	shlq	$0x2, %r8
1400b760f:     	movq	0x28(%rsp), %rax
1400b7614:     	movq	0x30(%rax), %rax
1400b7618:     	movq	(%rax), %rdx
1400b761b:     	callq	0x140171b50 <memmove>
1400b7620:     	jmp	0x1400b6bd0 <bool easyknack::CompiledInstanceAudio::process<easyknack::LocalBakedGroup6920b561e015::CompiledInstance>(easyknack::MultipleGraphNode&, easyknack::ProcessContext&)+0x2b0>
1400b7625:     	int3
1400b7626:     	int3
1400b7627:     	int3
1400b7628:     	int3
1400b7629:     	int3
1400b762a:     	int3
1400b762b:     	int3
1400b762c:     	int3
1400b762d:     	int3
1400b762e:     	int3
1400b762f:     	int3

