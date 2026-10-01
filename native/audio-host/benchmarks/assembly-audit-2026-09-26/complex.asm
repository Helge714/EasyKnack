000000014006b5b0 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)>:
14006b5b0:     	pushq	%r15
14006b5b2:     	pushq	%r14
14006b5b4:     	pushq	%r13
14006b5b6:     	pushq	%r12
14006b5b8:     	pushq	%rsi
14006b5b9:     	pushq	%rdi
14006b5ba:     	pushq	%rbp
14006b5bb:     	pushq	%rbx
14006b5bc:     	subq	$0x108, %rsp            # imm = 0x108
14006b5c3:     	movapd	%xmm15, 0xf0(%rsp)
14006b5cd:     	movapd	%xmm14, 0xe0(%rsp)
14006b5d7:     	movapd	%xmm13, 0xd0(%rsp)
14006b5e1:     	movapd	%xmm12, 0xc0(%rsp)
14006b5eb:     	movapd	%xmm11, 0xb0(%rsp)
14006b5f5:     	movapd	%xmm10, 0xa0(%rsp)
14006b5ff:     	movapd	%xmm9, 0x90(%rsp)
14006b609:     	movaps	%xmm8, 0x80(%rsp)
14006b612:     	movapd	%xmm7, 0x70(%rsp)
14006b618:     	movapd	%xmm6, 0x60(%rsp)
14006b61e:     	movq	%rdx, %rdi
14006b621:     	movq	%rcx, %rsi
14006b624:     	movq	0x8(%rdx), %rcx
14006b628:     	testq	%rcx, %rcx
14006b62b:     	je	0x14006b640 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x90>
14006b62d:     	movq	(%rdi), %rax
14006b630:     	movq	(%rax), %r14
14006b633:     	cmpq	$0x1, %rcx
14006b637:     	jne	0x14006b64a <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x9a>
14006b639:     	xorl	%r13d, %r13d
14006b63c:     	xorl	%ebx, %ebx
14006b63e:     	jmp	0x14006b66c <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0xbc>
14006b640:     	xorl	%r13d, %r13d
14006b643:     	xorl	%ebx, %ebx
14006b645:     	xorl	%r14d, %r14d
14006b648:     	jmp	0x14006b66c <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0xbc>
14006b64a:     	movq	(%rdi), %rax
14006b64d:     	movq	0x8(%rax), %rbx
14006b651:     	cmpq	$0x3, %rcx
14006b655:     	jb	0x14006b669 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0xb9>
14006b657:     	movq	(%rdi), %rax
14006b65a:     	movq	0x10(%rax), %r12
14006b65e:     	jne	0x14006bf5c <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x9ac>
14006b664:     	xorl	%r13d, %r13d
14006b667:     	jmp	0x14006b66f <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0xbf>
14006b669:     	xorl	%r13d, %r13d
14006b66c:     	xorl	%r12d, %r12d
14006b66f:     	movq	$0x0, 0x20(%rsp)
14006b678:     	movl	0x16e8(%rsi), %ebp
14006b67e:     	movl	0x16d4(%rsi), %eax
14006b684:     	cmpl	%ebp, %eax
14006b686:     	cmovbel	%ebp, %eax
14006b689:     	movl	%eax, 0x16d4(%rsi)
14006b68f:     	cmpb	$0x1, 0xa0(%rdi)
14006b696:     	jne	0x14006be17 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x867>
14006b69c:     	movsd	0x38(%rsi), %xmm8
14006b6a2:     	cmpq	$0x0, 0x18(%rdi)
14006b6a7:     	je	0x14006be4e <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x89e>
14006b6ad:     	movapd	%xmm8, %xmm0
14006b6b2:     	unpcklpd	%xmm8, %xmm0            # xmm0 = xmm0[0],xmm8[0]
14006b6b7:     	movapd	%xmm0, 0x50(%rsp)
14006b6bd:     	xorl	%r15d, %r15d
14006b6c0:     	movsd	0x10b51f(%rip), %xmm12  # 0x140176be8 <.refptr._newmode+0x3b60>
14006b6c9:     	movapd	0x10b56e(%rip), %xmm9   # 0x140176c40 <.refptr._newmode+0x3bb8>
14006b6d2:     	movq	%r14, 0x48(%rsp)
14006b6d7:     	movq	%rbx, 0x40(%rsp)
14006b6dc:     	movq	%r13, 0x30(%rsp)
14006b6e1:     	movq	%r12, 0x38(%rsp)
14006b6e6:     	movq	%rbp, 0x28(%rsp)
14006b6eb:     	jmp	0x14006b725 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x175>
14006b6ed:     	nopl	(%rax)
14006b6f0:     	xorps	%xmm0, %xmm0
14006b6f3:     	cvtsd2ss	%xmm1, %xmm0
14006b6f7:     	movq	0x10(%rdi), %rax
14006b6fb:     	movss	%xmm0, (%rax,%r15,4)
14006b701:     	incq	%r15
14006b704:     	movq	0x18(%rdi), %rcx
14006b708:     	cmpq	%rcx, %r15
14006b70b:     	movq	0x40(%rsp), %rbx
14006b710:     	movq	0x38(%rsp), %r12
14006b715:     	movq	0x30(%rsp), %r13
14006b71a:     	movq	0x28(%rsp), %rbp
14006b71f:     	jae	0x14006be50 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x8a0>
14006b725:     	cvtps2pd	0x16f8(%rsi), %xmm13
14006b72d:     	movupd	0x16d8(%rsi), %xmm0
14006b735:     	subpd	%xmm0, %xmm13
14006b73a:     	mulpd	0x50(%rsp), %xmm13
14006b741:     	addpd	%xmm0, %xmm13
14006b746:     	movupd	%xmm13, 0x16d8(%rsi)
14006b74f:     	testq	%rbx, %rbx
14006b752:     	je	0x14006b770 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x1c0>
14006b754:     	movss	(%rbx,%r15,4), %xmm0
14006b75a:     	xorps	%xmm14, %xmm14
14006b75e:     	cvtss2sd	%xmm0, %xmm14
14006b763:     	jmp	0x14006b775 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x1c5>
14006b765:     	nopw	%cs:(%rax,%rax)
14006b770:     	movapd	%xmm13, %xmm14
14006b775:     	movq	0x20(%rsp), %rcx
14006b77a:     	movq	%xmm14, %rax
14006b77f:     	movabsq	$0x7fffffffffffffff, %rdx # imm = 0x7FFFFFFFFFFFFFFF
14006b789:     	andq	%rdx, %rax
14006b78c:     	xorpd	%xmm6, %xmm6
14006b790:     	movabsq	$0x7ff0000000000000, %r8 # imm = 0x7FF0000000000000
14006b79a:     	cmpq	%r8, %rax
14006b79d:     	jl	0x14006b7a3 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x1f3>
14006b79f:     	xorps	%xmm14, %xmm14
14006b7a3:     	testq	%r12, %r12
14006b7a6:     	je	0x14006b7c0 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x210>
14006b7a8:     	movss	(%r12,%r15,4), %xmm0
14006b7ae:     	xorps	%xmm13, %xmm13
14006b7b2:     	cvtss2sd	%xmm0, %xmm13
14006b7b7:     	jmp	0x14006b7c5 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x215>
14006b7b9:     	nopl	(%rax)
14006b7c0:     	unpckhpd	%xmm13, %xmm13          # xmm13 = xmm13[1,1]
14006b7c5:     	movq	%xmm13, %rax
14006b7ca:     	andq	%rdx, %rax
14006b7cd:     	divsd	0x10b3f2(%rip), %xmm13  # 0x140176bc8 <.refptr._newmode+0x3b40>
14006b7d6:     	cmpq	%r8, %rax
14006b7d9:     	jl	0x14006b7e0 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x230>
14006b7db:     	xorpd	%xmm13, %xmm13
14006b7e0:     	testq	%rcx, %rcx
14006b7e3:     	je	0x14006b890 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x2e0>
14006b7e9:     	movss	(%rcx,%r15,4), %xmm0
14006b7ef:     	cvtss2sd	%xmm0, %xmm0
14006b7f3:     	movq	%xmm0, %rax
14006b7f8:     	andq	%rdx, %rax
14006b7fb:     	xorpd	%xmm1, %xmm1
14006b7ff:     	cmpq	%r8, %rax
14006b802:     	jl	0x14006b807 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x257>
14006b804:     	xorps	%xmm0, %xmm0
14006b807:     	movsd	0x10b3c1(%rip), %xmm2   # 0x140176bd0 <.refptr._newmode+0x3b48>
14006b80f:     	minsd	%xmm0, %xmm2
14006b813:     	cmpltsd	%xmm1, %xmm0
14006b818:     	andnpd	%xmm2, %xmm0
14006b81c:     	xorps	%xmm1, %xmm1
14006b81f:     	cvtsd2ss	%xmm0, %xmm1
14006b823:     	testq	%r13, %r13
14006b826:     	je	0x14006b89d <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x2ed>
14006b828:     	movss	(%r13,%r15,4), %xmm0
14006b82f:     	cvtss2sd	%xmm0, %xmm0
14006b833:     	movq	%xmm0, %rax
14006b838:     	andq	%rdx, %rax
14006b83b:     	cmpq	%r8, %rax
14006b83e:     	jl	0x14006b843 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x293>
14006b840:     	xorps	%xmm0, %xmm0
14006b843:     	movsd	0x10b38d(%rip), %xmm2   # 0x140176bd8 <.refptr._newmode+0x3b50>
14006b84b:     	minsd	%xmm0, %xmm2
14006b84f:     	movsd	0x10b389(%rip), %xmm4   # 0x140176be0 <.refptr._newmode+0x3b58>
14006b857:     	cmpltsd	%xmm4, %xmm0
14006b85c:     	movapd	%xmm0, %xmm3
14006b860:     	andnpd	%xmm2, %xmm3
14006b864:     	andpd	%xmm4, %xmm0
14006b868:     	orpd	%xmm3, %xmm0
14006b86c:     	xorps	%xmm2, %xmm2
14006b86f:     	cvtsd2ss	%xmm0, %xmm2
14006b873:     	ucomiss	0x16c8(%rsi), %xmm1
14006b87a:     	jne	0x14006bb00 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x550>
14006b880:     	jnp	0x14006b8b8 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x308>
14006b882:     	jmp	0x14006bb00 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x550>
14006b887:     	nopw	(%rax,%rax)
14006b890:     	movss	0x16f0(%rsi), %xmm1
14006b898:     	testq	%r13, %r13
14006b89b:     	jne	0x14006b828 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x278>
14006b89d:     	movss	0x16f4(%rsi), %xmm2
14006b8a5:     	ucomiss	0x16c8(%rsi), %xmm1
14006b8ac:     	jne	0x14006bb00 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x550>
14006b8b2:     	jp	0x14006bb00 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x550>
14006b8b8:     	ucomiss	0x16cc(%rsi), %xmm2
14006b8bf:     	jne	0x14006bb00 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x550>
14006b8c5:     	jp	0x14006bb00 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x550>
14006b8cb:     	cmpl	0x16d0(%rsi), %ebp
14006b8d1:     	jne	0x14006bb00 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x550>
14006b8d7:     	movl	0x16d4(%rsi), %ebp
14006b8dd:     	testq	%rbp, %rbp
14006b8e0:     	je	0x14006bb1a <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x56a>
14006b8e6:     	movsd	0x10b2d2(%rip), %xmm0   # 0x140176bc0 <.refptr._newmode+0x3b38>
14006b8ee:     	minsd	%xmm14, %xmm0
14006b8f3:     	cmpltsd	%xmm6, %xmm14
14006b8f9:     	andnpd	%xmm0, %xmm14
14006b8fe:     	testq	%r14, %r14
14006b901:     	je	0x14006bb50 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x5a0>
14006b907:     	movzbl	0xa0(%rdi), %ebx
14006b90e:     	xorpd	%xmm15, %xmm15
14006b913:     	xorl	%r12d, %r12d
14006b916:     	jmp	0x14006b9e8 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x438>
14006b91b:     	nopl	(%rax,%rax)
14006b920:     	xorl	%edx, %edx
14006b922:     	divq	%r13
14006b925:     	movss	(%r14,%rdx,4), %xmm2
14006b92b:     	unpcklps	%xmm1, %xmm2            # xmm2 = xmm2[0],xmm1[0],xmm2[1],xmm1[1]
14006b92e:     	cvtps2pd	%xmm2, %xmm2
14006b931:     	movapd	%xmm2, %xmm1
14006b935:     	andpd	0x10b2e3(%rip), %xmm1   # 0x140176c20 <.refptr._newmode+0x3b98>
14006b93d:     	movapd	%xmm1, %xmm3
14006b941:     	movapd	0x10b2e7(%rip), %xmm4   # 0x140176c30 <.refptr._newmode+0x3ba8>
14006b949:     	cmpneqpd	%xmm4, %xmm3
14006b94e:     	cmpordpd	%xmm4, %xmm1
14006b953:     	andpd	%xmm3, %xmm1
14006b957:     	andpd	%xmm2, %xmm1
14006b95b:     	movapd	0x10b2ed(%rip), %xmm2   # 0x140176c50 <.refptr._newmode+0x3bc8>
14006b963:     	minpd	%xmm1, %xmm2
14006b967:     	cmpltpd	%xmm9, %xmm1
14006b96d:     	movapd	%xmm9, %xmm3
14006b972:     	andpd	%xmm1, %xmm3
14006b976:     	andnpd	%xmm2, %xmm1
14006b97a:     	orpd	%xmm3, %xmm1
14006b97e:     	pshufd	$0xee, %xmm1, %xmm2     # xmm2 = xmm1[2,3,2,3]
14006b983:     	subsd	%xmm2, %xmm1
14006b987:     	movq	%rcx, %xmm3
14006b98c:     	punpckldq	0x10b25c(%rip), %xmm3 # xmm3 = xmm3[0],mem[0],xmm3[1],mem[1]
                                                # 0x140176bf0 <.refptr._newmode+0x3b68>
14006b994:     	subpd	0x10b264(%rip), %xmm3   # 0x140176c00 <.refptr._newmode+0x3b78>
14006b99c:     	movapd	%xmm3, %xmm4
14006b9a0:     	unpckhpd	%xmm3, %xmm4            # xmm4 = xmm4[1],xmm3[1]
14006b9a4:     	addsd	%xmm3, %xmm4
14006b9a8:     	subsd	%xmm4, %xmm0
14006b9ac:     	mulsd	%xmm1, %xmm0
14006b9b0:     	addsd	%xmm2, %xmm0
14006b9b4:     	mulsd	%xmm10, %xmm0
14006b9b9:     	unpcklpd	%xmm10, %xmm0           # xmm0 = xmm0[0],xmm10[0]
14006b9be:     	addpd	%xmm0, %xmm15
14006b9c3:     	addsd	%xmm11, %xmm6
14006b9c8:     	movapd	%xmm6, %xmm0
14006b9cc:     	callq	0x140171a40 <floor>
14006b9d1:     	subsd	%xmm0, %xmm6
14006b9d5:     	movsd	%xmm6, 0x48(%rsi,%r12,8)
14006b9dc:     	incq	%r12
14006b9df:     	cmpq	%r12, %rbp
14006b9e2:     	je	0x14006bb1f <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x56f>
14006b9e8:     	movsd	0x4c8(%rsi,%r12,8), %xmm7
14006b9f2:     	movsd	0xdc8(%rsi,%r12,8), %xmm0
14006b9fc:     	subsd	%xmm0, %xmm7
14006ba00:     	mulsd	%xmm8, %xmm7
14006ba05:     	addsd	%xmm0, %xmm7
14006ba09:     	movsd	%xmm7, 0xdc8(%rsi,%r12,8)
14006ba13:     	movsd	0x948(%rsi,%r12,8), %xmm10
14006ba1d:     	movsd	0x1248(%rsi,%r12,8), %xmm0
14006ba27:     	subsd	%xmm0, %xmm10
14006ba2c:     	mulsd	%xmm8, %xmm10
14006ba31:     	addsd	%xmm0, %xmm10
14006ba36:     	movsd	%xmm10, 0x1248(%rsi,%r12,8)
14006ba40:     	movapd	%xmm14, %xmm6
14006ba45:     	mulsd	%xmm7, %xmm6
14006ba49:     	divsd	0x20(%rdi), %xmm6
14006ba4e:     	ucomisd	%xmm6, %xmm12
14006ba53:     	movsd	0x48(%rsi,%r12,8), %xmm11
14006ba5a:     	jbe	0x14006b9c3 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x413>
14006ba60:     	mulsd	%xmm13, %xmm7
14006ba65:     	addsd	%xmm11, %xmm7
14006ba6a:     	leaq	0x30(%rdi), %rax
14006ba6e:     	leaq	0x18(%rdi), %rcx
14006ba72:     	testb	$0x1, %bl
14006ba75:     	cmovneq	%rax, %rcx
14006ba79:     	movq	(%rcx), %r13
14006ba7c:     	movapd	%xmm7, %xmm0
14006ba80:     	callq	0x140171a40 <floor>
14006ba85:     	subsd	%xmm0, %xmm7
14006ba89:     	movq	%r13, %xmm1
14006ba8e:     	punpckldq	0x10b15a(%rip), %xmm1 # xmm1 = xmm1[0],mem[0],xmm1[1],mem[1]
                                                # 0x140176bf0 <.refptr._newmode+0x3b68>
14006ba96:     	subpd	0x10b162(%rip), %xmm1   # 0x140176c00 <.refptr._newmode+0x3b78>
14006ba9e:     	movapd	%xmm1, %xmm0
14006baa2:     	unpckhpd	%xmm1, %xmm0            # xmm0 = xmm0[1],xmm1[1]
14006baa6:     	addsd	%xmm1, %xmm0
14006baaa:     	mulsd	%xmm7, %xmm0
14006baae:     	cvttsd2si	%xmm0, %rax
14006bab3:     	movq	%rax, %rdx
14006bab6:     	movapd	%xmm0, %xmm1
14006baba:     	subsd	0x10b14e(%rip), %xmm1   # 0x140176c10 <.refptr._newmode+0x3b88>
14006bac2:     	cvttsd2si	%xmm1, %rcx
14006bac7:     	sarq	$0x3f, %rdx
14006bacb:     	andq	%rdx, %rcx
14006bace:     	orq	%rax, %rcx
14006bad1:     	movss	(%r14,%rcx,4), %xmm1
14006bad7:     	leaq	0x1(%rcx), %rax
14006badb:     	movq	%rax, %rdx
14006bade:     	orq	%r13, %rdx
14006bae1:     	shrq	$0x20, %rdx
14006bae5:     	jne	0x14006b920 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x370>
14006baeb:     	xorl	%edx, %edx
14006baed:     	divl	%r13d
14006baf0:     	jmp	0x14006b925 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x375>
14006baf5:     	nopw	%cs:(%rax,%rax)
14006bb00:     	movq	%rsi, %rcx
14006bb03:     	movl	%ebp, %r9d
14006bb06:     	callq	0x14006d3b0 <easyknack::AdditiveOscillatorNode::updateSpectrum(float, float, unsigned int)>
14006bb0b:     	movl	0x16d4(%rsi), %ebp
14006bb11:     	testq	%rbp, %rbp
14006bb14:     	jne	0x14006b8e6 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x336>
14006bb1a:     	xorpd	%xmm15, %xmm15
14006bb1f:     	movapd	%xmm15, %xmm0
14006bb24:     	unpckhpd	%xmm15, %xmm0           # xmm0 = xmm0[1],xmm15[1]
14006bb29:     	xorpd	%xmm1, %xmm1
14006bb2d:     	ucomisd	%xmm1, %xmm0
14006bb31:     	jbe	0x14006b6f0 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x140>
14006bb37:     	divsd	%xmm0, %xmm15
14006bb3c:     	movapd	%xmm15, %xmm1
14006bb41:     	jmp	0x14006b6f0 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x140>
14006bb46:     	nopw	%cs:(%rax,%rax)
14006bb50:     	movl	0x16ec(%rsi), %ebx
14006bb56:     	imulq	$0x14028, %rbx, %r12    # imm = 0x14028
14006bb5d:     	addq	0x40(%rsi), %r12
14006bb61:     	xorpd	%xmm15, %xmm15
14006bb66:     	xorl	%r13d, %r13d
14006bb69:     	jmp	0x14006bc95 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x6e5>
14006bb6e:     	xorl	%eax, %eax
14006bb70:     	mulsd	%xmm13, %xmm6
14006bb75:     	addsd	%xmm11, %xmm6
14006bb7a:     	movl	%eax, %ecx
14006bb7c:     	shll	$0xd, %ecx
14006bb7f:     	leal	(%rcx,%rax,4), %r14d
14006bb83:     	addq	%r12, %r14
14006bb86:     	movapd	%xmm6, %xmm0
14006bb8a:     	callq	0x140171a40 <floor>
14006bb8f:     	subsd	%xmm0, %xmm6
14006bb93:     	mulsd	0x10b0fd(%rip), %xmm6   # 0x140176c98 <.refptr._newmode+0x3c10>
14006bb9b:     	cvttsd2si	%xmm6, %rcx
14006bba0:     	movq	%rcx, %rdx
14006bba3:     	sarq	$0x3f, %rdx
14006bba7:     	movapd	%xmm6, %xmm0
14006bbab:     	subsd	0x10b05d(%rip), %xmm0   # 0x140176c10 <.refptr._newmode+0x3b88>
14006bbb3:     	cvttsd2si	%xmm0, %rax
14006bbb8:     	andq	%rdx, %rax
14006bbbb:     	orq	%rcx, %rax
14006bbbe:     	leal	0x1(%rax), %ecx
14006bbc1:     	andl	$0x7ff, %ecx            # imm = 0x7FF
14006bbc7:     	movss	(%r14,%rax,4), %xmm0
14006bbcd:     	movss	(%r14,%rcx,4), %xmm1
14006bbd3:     	unpcklps	%xmm0, %xmm1            # xmm1 = xmm1[0],xmm0[0],xmm1[1],xmm0[1]
14006bbd6:     	cvtps2pd	%xmm1, %xmm1
14006bbd9:     	movapd	%xmm1, %xmm0
14006bbdd:     	andpd	0x10b03b(%rip), %xmm0   # 0x140176c20 <.refptr._newmode+0x3b98>
14006bbe5:     	movapd	%xmm0, %xmm2
14006bbe9:     	movapd	0x10b03f(%rip), %xmm3   # 0x140176c30 <.refptr._newmode+0x3ba8>
14006bbf1:     	cmpneqpd	%xmm3, %xmm2
14006bbf6:     	cmpordpd	%xmm3, %xmm0
14006bbfb:     	andpd	%xmm2, %xmm0
14006bbff:     	andpd	%xmm1, %xmm0
14006bc03:     	movapd	0x10b045(%rip), %xmm1   # 0x140176c50 <.refptr._newmode+0x3bc8>
14006bc0b:     	minpd	%xmm0, %xmm1
14006bc0f:     	cmpltpd	%xmm9, %xmm0
14006bc15:     	movapd	%xmm9, %xmm2
14006bc1a:     	andpd	%xmm0, %xmm2
14006bc1e:     	andnpd	%xmm1, %xmm0
14006bc22:     	orpd	%xmm2, %xmm0
14006bc26:     	pshufd	$0xee, %xmm0, %xmm1     # xmm1 = xmm0[2,3,2,3]
14006bc2b:     	subsd	%xmm1, %xmm0
14006bc2f:     	movq	%rax, %xmm2
14006bc34:     	punpckldq	0x10afb4(%rip), %xmm2 # xmm2 = xmm2[0],mem[0],xmm2[1],mem[1]
                                                # 0x140176bf0 <.refptr._newmode+0x3b68>
14006bc3c:     	subpd	0x10afbc(%rip), %xmm2   # 0x140176c00 <.refptr._newmode+0x3b78>
14006bc44:     	movapd	%xmm2, %xmm3
14006bc48:     	unpckhpd	%xmm2, %xmm3            # xmm3 = xmm3[1],xmm2[1]
14006bc4c:     	addsd	%xmm2, %xmm3
14006bc50:     	subsd	%xmm3, %xmm6
14006bc54:     	mulsd	%xmm0, %xmm6
14006bc58:     	addsd	%xmm1, %xmm6
14006bc5c:     	mulsd	%xmm10, %xmm6
14006bc61:     	unpcklpd	%xmm10, %xmm6           # xmm6 = xmm6[0],xmm10[0]
14006bc66:     	addpd	%xmm6, %xmm15
14006bc6b:     	movq	0x48(%rsp), %r14
14006bc70:     	addsd	%xmm11, %xmm7
14006bc75:     	movapd	%xmm7, %xmm0
14006bc79:     	callq	0x140171a40 <floor>
14006bc7e:     	subsd	%xmm0, %xmm7
14006bc82:     	movsd	%xmm7, 0x48(%rsi,%r13,8)
14006bc89:     	incq	%r13
14006bc8c:     	cmpq	%r13, %rbp
14006bc8f:     	je	0x14006bb1f <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x56f>
14006bc95:     	movsd	0x4c8(%rsi,%r13,8), %xmm6
14006bc9f:     	movsd	0xdc8(%rsi,%r13,8), %xmm0
14006bca9:     	subsd	%xmm0, %xmm6
14006bcad:     	mulsd	%xmm8, %xmm6
14006bcb2:     	addsd	%xmm0, %xmm6
14006bcb6:     	movsd	%xmm6, 0xdc8(%rsi,%r13,8)
14006bcc0:     	movsd	0x948(%rsi,%r13,8), %xmm10
14006bcca:     	movsd	0x1248(%rsi,%r13,8), %xmm0
14006bcd4:     	subsd	%xmm0, %xmm10
14006bcd9:     	mulsd	%xmm8, %xmm10
14006bcde:     	addsd	%xmm0, %xmm10
14006bce3:     	movsd	%xmm10, 0x1248(%rsi,%r13,8)
14006bced:     	movapd	%xmm14, %xmm7
14006bcf2:     	mulsd	%xmm6, %xmm7
14006bcf6:     	divsd	0x20(%rdi), %xmm7
14006bcfb:     	ucomisd	%xmm7, %xmm12
14006bd00:     	movsd	0x48(%rsi,%r13,8), %xmm11
14006bd07:     	jbe	0x14006bc70 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x6c0>
14006bd0d:     	testl	%ebx, %ebx
14006bd0f:     	je	0x14006bb6e <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x5be>
14006bd15:     	movapd	%xmm7, %xmm0
14006bd19:     	addsd	%xmm7, %xmm0
14006bd1d:     	ucomisd	%xmm0, %xmm12
14006bd22:     	movl	$0x0, %eax
14006bd27:     	jbe	0x14006bb70 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x5c0>
14006bd2d:     	movapd	%xmm7, %xmm0
14006bd31:     	mulsd	0x10ae97(%rip), %xmm0   # 0x140176bd0 <.refptr._newmode+0x3b48>
14006bd39:     	movl	$0x1, %eax
14006bd3e:     	ucomisd	%xmm0, %xmm12
14006bd43:     	jbe	0x14006bb70 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x5c0>
14006bd49:     	movapd	%xmm7, %xmm0
14006bd4d:     	mulsd	0x10af0b(%rip), %xmm0   # 0x140176c60 <.refptr._newmode+0x3bd8>
14006bd55:     	movl	$0x2, %eax
14006bd5a:     	ucomisd	%xmm0, %xmm12
14006bd5f:     	jbe	0x14006bb70 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x5c0>
14006bd65:     	movapd	%xmm7, %xmm0
14006bd69:     	mulsd	0x10aef7(%rip), %xmm0   # 0x140176c68 <.refptr._newmode+0x3be0>
14006bd71:     	movl	$0x3, %eax
14006bd76:     	ucomisd	%xmm0, %xmm12
14006bd7b:     	jbe	0x14006bb70 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x5c0>
14006bd81:     	movapd	%xmm7, %xmm0
14006bd85:     	mulsd	0x10aee3(%rip), %xmm0   # 0x140176c70 <.refptr._newmode+0x3be8>
14006bd8d:     	movl	$0x4, %eax
14006bd92:     	ucomisd	%xmm0, %xmm12
14006bd97:     	jbe	0x14006bb70 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x5c0>
14006bd9d:     	movapd	%xmm7, %xmm0
14006bda1:     	mulsd	0x10aecf(%rip), %xmm0   # 0x140176c78 <.refptr._newmode+0x3bf0>
14006bda9:     	movl	$0x5, %eax
14006bdae:     	ucomisd	%xmm0, %xmm12
14006bdb3:     	jbe	0x14006bb70 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x5c0>
14006bdb9:     	movapd	%xmm7, %xmm0
14006bdbd:     	mulsd	0x10aebb(%rip), %xmm0   # 0x140176c80 <.refptr._newmode+0x3bf8>
14006bdc5:     	movl	$0x6, %eax
14006bdca:     	ucomisd	%xmm0, %xmm12
14006bdcf:     	jbe	0x14006bb70 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x5c0>
14006bdd5:     	movapd	%xmm7, %xmm0
14006bdd9:     	mulsd	0x10aea7(%rip), %xmm0   # 0x140176c88 <.refptr._newmode+0x3c00>
14006bde1:     	movl	$0x7, %eax
14006bde6:     	ucomisd	%xmm0, %xmm12
14006bdeb:     	jbe	0x14006bb70 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x5c0>
14006bdf1:     	movapd	%xmm7, %xmm0
14006bdf5:     	mulsd	0x10ae93(%rip), %xmm0   # 0x140176c90 <.refptr._newmode+0x3c08>
14006bdfd:     	movl	$0x8, %eax
14006be02:     	ucomisd	%xmm0, %xmm12
14006be07:     	jbe	0x14006bb70 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x5c0>
14006be0d:     	movl	$0x9, %eax
14006be12:     	jmp	0x14006bb70 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x5c0>
14006be17:     	movsd	0x20(%rdi), %xmm1
14006be1c:     	mulsd	0x10ad8c(%rip), %xmm1   # 0x140176bb0 <.refptr._newmode+0x3b28>
14006be24:     	movsd	0x10ad8c(%rip), %xmm0   # 0x140176bb8 <.refptr._newmode+0x3b30>
14006be2c:     	divsd	%xmm1, %xmm0
14006be30:     	callq	0x1401719f0 <exp>
14006be35:     	movsd	0x10ad6a(%rip), %xmm8   # 0x140176ba8 <.refptr._newmode+0x3b20>
14006be3e:     	subsd	%xmm0, %xmm8
14006be43:     	cmpq	$0x0, 0x18(%rdi)
14006be48:     	jne	0x14006b6ad <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0xfd>
14006be4e:     	xorl	%ecx, %ecx
14006be50:     	movl	0x16d4(%rsi), %eax
14006be56:     	cmpb	$0x1, 0xa0(%rdi)
14006be5d:     	jne	0x14006beb8 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x908>
14006be5f:     	addq	0x98(%rdi), %rcx
14006be66:     	cmpq	0x30(%rdi), %rcx
14006be6a:     	jne	0x14006bef6 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x946>
14006be70:     	cmpl	%ebp, %eax
14006be72:     	jbe	0x14006bef6 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x946>
14006be78:     	leal	-0x1(%rax), %ecx
14006be7b:     	movsd	0x10ae1d(%rip), %xmm0   # 0x140176ca0 <.refptr._newmode+0x3c18>
14006be83:     	nopw	%cs:(%rax,%rax)
14006be90:     	ucomisd	0x1248(%rsi,%rcx,8), %xmm0
14006be99:     	jbe	0x14006bef6 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x946>
14006be9b:     	movl	%ecx, 0x16d4(%rsi)
14006bea1:     	movq	$0x0, 0x1248(%rsi,%rcx,8)
14006bead:     	decq	%rcx
14006beb0:     	decl	%eax
14006beb2:     	cmpl	%eax, %ebp
14006beb4:     	jb	0x14006be90 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x8e0>
14006beb6:     	jmp	0x14006bef6 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x946>
14006beb8:     	cmpl	%ebp, %eax
14006beba:     	jbe	0x14006bef6 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x946>
14006bebc:     	leal	-0x1(%rax), %ecx
14006bebf:     	movsd	0x10add9(%rip), %xmm0   # 0x140176ca0 <.refptr._newmode+0x3c18>
14006bec7:     	nopw	(%rax,%rax)
14006bed0:     	ucomisd	0x1248(%rsi,%rcx,8), %xmm0
14006bed9:     	jbe	0x14006bef6 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x946>
14006bedb:     	movl	%ecx, 0x16d4(%rsi)
14006bee1:     	movq	$0x0, 0x1248(%rsi,%rcx,8)
14006beed:     	decq	%rcx
14006bef0:     	decl	%eax
14006bef2:     	cmpl	%eax, %ebp
14006bef4:     	jb	0x14006bed0 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x920>
14006bef6:     	movaps	0x60(%rsp), %xmm6
14006befb:     	movaps	0x70(%rsp), %xmm7
14006bf00:     	movaps	0x80(%rsp), %xmm8
14006bf09:     	movaps	0x90(%rsp), %xmm9
14006bf12:     	movaps	0xa0(%rsp), %xmm10
14006bf1b:     	movaps	0xb0(%rsp), %xmm11
14006bf24:     	movaps	0xc0(%rsp), %xmm12
14006bf2d:     	movaps	0xd0(%rsp), %xmm13
14006bf36:     	movaps	0xe0(%rsp), %xmm14
14006bf3f:     	movaps	0xf0(%rsp), %xmm15
14006bf48:     	addq	$0x108, %rsp            # imm = 0x108
14006bf4f:     	popq	%rbx
14006bf50:     	popq	%rbp
14006bf51:     	popq	%rdi
14006bf52:     	popq	%rsi
14006bf53:     	popq	%r12
14006bf55:     	popq	%r13
14006bf57:     	popq	%r14
14006bf59:     	popq	%r15
14006bf5b:     	retq
14006bf5c:     	movq	(%rdi), %rax
14006bf5f:     	movq	0x18(%rax), %rax
14006bf63:     	cmpq	$0x5, %rcx
14006bf67:     	jb	0x14006bf84 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x9d4>
14006bf69:     	movq	(%rdi), %rcx
14006bf6c:     	movq	0x20(%rcx), %rcx
14006bf70:     	movq	%rcx, 0x20(%rsp)
14006bf75:     	jne	0x14006bf9a <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x9ea>
14006bf77:     	xorl	%r13d, %r13d
14006bf7a:     	testq	%rax, %rax
14006bf7d:     	jne	0x14006bfaa <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x9fa>
14006bf7f:     	jmp	0x14006b678 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0xc8>
14006bf84:     	movq	$0x0, 0x20(%rsp)
14006bf8d:     	xorl	%r13d, %r13d
14006bf90:     	testq	%rax, %rax
14006bf93:     	jne	0x14006bfaa <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0x9fa>
14006bf95:     	jmp	0x14006b678 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0xc8>
14006bf9a:     	movq	(%rdi), %rcx
14006bf9d:     	movq	0x28(%rcx), %r13
14006bfa1:     	testq	%rax, %rax
14006bfa4:     	je	0x14006b678 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0xc8>
14006bfaa:     	movss	(%rax), %xmm0
14006bfae:     	cvtss2sd	%xmm0, %xmm0
14006bfb2:     	movq	%xmm0, %rax
14006bfb7:     	movabsq	$0x7fffffffffffffff, %rcx # imm = 0x7FFFFFFFFFFFFFFF
14006bfc1:     	andq	%rax, %rcx
14006bfc4:     	movabsq	$0x7ff0000000000000, %rax # imm = 0x7FF0000000000000
14006bfce:     	cmpq	%rax, %rcx
14006bfd1:     	jl	0x14006bfd6 <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0xa26>
14006bfd3:     	xorps	%xmm0, %xmm0
14006bfd6:     	movsd	0x10abc2(%rip), %xmm1   # 0x140176ba0 <.refptr._newmode+0x3b18>
14006bfde:     	minsd	%xmm0, %xmm1
14006bfe2:     	movsd	0x10abbe(%rip), %xmm2   # 0x140176ba8 <.refptr._newmode+0x3b20>
14006bfea:     	cmpltsd	%xmm2, %xmm0
14006bfef:     	movapd	%xmm0, %xmm3
14006bff3:     	andnpd	%xmm1, %xmm3
14006bff7:     	andpd	%xmm2, %xmm0
14006bffb:     	orpd	%xmm3, %xmm0
14006bfff:     	cvttsd2si	%xmm0, %rbp
14006c004:     	jmp	0x14006b67e <easyknack::AdditiveOscillatorNode::process(easyknack::ProcessContext&)+0xce>
14006c009:     	int3
14006c00a:     	int3
14006c00b:     	int3
14006c00c:     	int3
14006c00d:     	int3
14006c00e:     	int3
14006c00f:     	int3

